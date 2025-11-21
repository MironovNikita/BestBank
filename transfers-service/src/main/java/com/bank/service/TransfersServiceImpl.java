package com.bank.service;

import com.bank.common.exception.TransferException;
import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.email.EmailNotificationDto;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.repository.TransfersRepository;
import com.bank.security.SecureBase64Converter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.bank.dto.email.EmailTemplates.TRANSFER_CHANGE_TEXT;
import static com.bank.dto.email.EmailTemplates.TRANSFER_OPERATION_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfersServiceImpl implements TransfersService {

    private final TransfersRepository transfersRepository;
    private final WebClient accountsWebClient;
    private final WebClient notificationsWebClient;
    private final TransferOperationMapper transferOperationMapper;
    private final SecureBase64Converter converter;
    private final Retry notificationsServiceRetry;
    private final CircuitBreaker notificationsServiceCB;
    private final Retry accountsServiceRetry;
    private final CircuitBreaker accountsServiceCB;

    @Override
    @Transactional
    public Mono<Void> operateTransfer(TransferOperationDto transferOperationDto) {
        TransferOperation transferOperation = transferOperationMapper.toTransferOperation(transferOperationDto);

        return accountsWebClient
                .post()
                .uri("/accounts/transfer")
                .bodyValue(transferOperationDto)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("4хх ошибка при обращении (запрос перевода средств) к accounts-service: {}", msg);
                                    return Mono.error(new TransferException());
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("5хх ошибка при обращении (запрос перевода средств) к accounts-service: {}", msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    return resp.releaseBody();
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                .then(Mono.defer(() -> {
                    log.info("Перевод c ID {} на ID {} выполнен успешно.", transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo());
                    String email = converter.decrypt(transferOperationDto.getEmail());
                    sendNotification(email, TRANSFER_OPERATION_SUBJECT, TRANSFER_CHANGE_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> log.error("Ошибка при отправке уведомления для {}: {}", email, ex.getMessage()))
                            .subscribe();
                    return transfersRepository.save(transferOperation);
                }))
                .then()
                .onErrorResume(ex -> {
                    if (ex instanceof TransferException) log.error("Перевод со счёта ID: {} на счёт ID: {} завершился с ошибкой: {}",
                            transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo(), ex.getMessage());

                    else log.error("Перевод со счёта ID: {} на счёт ID: {} завершился технической ошибкой: {}",
                            transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo(), ex.getMessage());

                    return Mono.error(ex);
                });
    }

    private Mono<Void> sendNotification(String toEmail, String subject, String text) {
        EmailNotificationDto email = new EmailNotificationDto(toEmail, subject, text);

        return notificationsWebClient
                .post()
                .uri("/email")
                .bodyValue(email)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("Ошибка при отправке уведомления на почту: {}", toEmail);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    log.info("Успешная отправка уведомления на почту: {}", toEmail);
                    return Mono.empty();
                })
                .transformDeferred(CircuitBreakerOperator.of(notificationsServiceCB))
                .transformDeferred(RetryOperator.of(notificationsServiceRetry))
                .then();
    }
}
