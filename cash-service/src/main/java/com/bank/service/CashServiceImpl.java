package com.bank.service;

import com.bank.common.exception.NotEnoughFundsException;
import com.bank.common.mapper.CashOperationMapper;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.CashOperationDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.email.EmailNotificationDto;
import com.bank.entity.CashOperation;
import com.bank.repository.CashRepository;
import com.bank.security.SecureBase64Converter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.bank.dto.email.EmailTemplates.CASH_OPERATION_SUBJECT;
import static com.bank.dto.email.EmailTemplates.CASH_OPERATION_TEXT;
import static com.bank.entity.OperationType.GET;
import static com.bank.entity.OperationType.PUT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final CashRepository cashRepository;
    private final WebClient accountsWebClient;
    private final WebClient notificationsWebClient;
    private final CashOperationMapper cashOperationMapper;
    private final SecureBase64Converter converter;
    private final Retry notificationsServiceRetry;
    private final CircuitBreaker notificationsServiceCB;
    private final Retry accountsServiceRetry;
    private final CircuitBreaker accountsServiceCB;

    @Override
    @Transactional
    public Mono<Void> operateCash(CashOperationDto cashOperationDto) {
        CashOperation cashOperation = cashOperationMapper.toCashOperation(cashOperationDto);

        return getCurrentBalance(cashOperation.getAccountId())
                .flatMap(balance -> calculateNewBalance(balance, cashOperation))
                .flatMap(newBalance -> updateRemoteBalance(newBalance, cashOperationDto.getAccountId())
                        .then(saveOperation(cashOperation)))
                .doOnSuccess(v -> {
                    log.info("Операция с наличными для пользователя {} выполнена.", cashOperation.getAccountId());

                    String email = converter.decrypt(cashOperationDto.getEmail());
                    sendNotification(email, CASH_OPERATION_SUBJECT, CASH_OPERATION_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> log.error("Ошибка при отправке уведомления для {}: {}", email, ex.getMessage()))
                            .subscribe();
                })
                .then();
    }

    private Mono<Long> getCurrentBalance(Long accountId) {
        return accountsWebClient
                .get()
                .uri("/accounts/{id}/balance", accountId)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("4хх ошибка при обращении (запрос баланса) к accounts-service: {}", msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }
                    return resp.bodyToMono(BalanceDto.class)
                            .map(BalanceDto::getBalance);
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                .onErrorMap(ex -> {
                    log.error("Ошибка получения баланса для пользователя с ID {}: {}", accountId, ex.getMessage());
                    return new RuntimeException("Ошибка получения баланса: " + ex.getMessage(), ex);
                });
    }

    private Mono<Long> calculateNewBalance(Long currentBalance, CashOperation cashOperation) {
        long amount = cashOperation.getAmount();

        if (cashOperation.getOperation() == GET) {
            if (amount > currentBalance) {
                return Mono.error(new NotEnoughFundsException(currentBalance));
            }
            return Mono.just(currentBalance - amount);
        } else if (cashOperation.getOperation() == PUT) {
            return Mono.just(currentBalance + amount);
        } else {
            return Mono.error(new RuntimeException("Неизвестный тип операции"));
        }
    }

    private Mono<Void> updateRemoteBalance(Long newBalance, Long accountId) {
        UpdateBalanceRq updateBalanceRq = new UpdateBalanceRq(newBalance);

        return accountsWebClient
                .post()
                .uri("/accounts/{id}/balance", accountId)
                .bodyValue(updateBalanceRq)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(msg -> {
                            log.error("Ошибка при обращении (обновление баланса) к accounts-service: {}", msg);
                            return Mono.error(new RuntimeException(msg));
                        }))
                .toBodilessEntity()
                .then()
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry));
    }

    private Mono<Void> saveOperation(CashOperation cashOperation) {
        return cashRepository.save(cashOperation).then();
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
