package com.bank.service;

import com.bank.common.exception.TransferException;
import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.email.EmailNotificationDto;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.repository.TransfersRepository;
import com.bank.security.SecureBase64Converter;
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

    @Override
    @Transactional
    public Mono<Void> operateTransfer(TransferOperationDto transferOperationDto) {
        TransferOperation transferOperation = transferOperationMapper.toTransferOperation(transferOperationDto);

        return accountsWebClient
                .post()
                .uri("/accounts/transfer")
                .bodyValue(transferOperationDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("Перевод c ID {} на ID {} выполнен успешно.", transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo()))
                .then(transfersRepository.save(transferOperation))
                .doOnSuccess(v -> {
                    String email = converter.decrypt(transferOperationDto.getEmail());
                    sendNotification(email, TRANSFER_OPERATION_SUBJECT, TRANSFER_CHANGE_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> log.error("Ошибка при отправке уведомления для {}: {}", email, ex.getMessage()))
                            .subscribe();
                })
                .then()
                .onErrorResume(ex -> {
                    log.error("Перевод со счёта ID: {} на счёт ID: {} завершился с ошибкой: {}", transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo(), ex.getMessage());
                    return Mono.error(new TransferException());
                });
    }

    private Mono<Void> sendNotification(String toEmail, String subject, String text) {
        EmailNotificationDto email = new EmailNotificationDto(toEmail, subject, text);

        return notificationsWebClient
                .post()
                .uri("/email")
                .bodyValue(email)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
