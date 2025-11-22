package com.bank.service;

import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.repository.TransfersRepository;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static com.bank.dto.email.EmailTemplates.TRANSFER_CHANGE_TEXT;
import static com.bank.dto.email.EmailTemplates.TRANSFER_OPERATION_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfersServiceImpl implements TransfersService {

    private final TransfersRepository transfersRepository;
    private final TransferOperationMapper transferOperationMapper;
    private final SecureBase64Converter converter;
    private final AccountsServiceClient accountsServiceClient;
    private final NotificationsServiceClient notificationsServiceClient;

    @Override
    @Transactional
    public Mono<Void> operateTransfer(TransferOperationDto dto) {
        TransferOperation operation = transferOperationMapper.toTransferOperation(dto);

        return accountsServiceClient.transfer(dto)
                .then(Mono.defer(() ->
                        notificationsServiceClient.sendTransferNotification(converter.decrypt(dto.getEmail()), TRANSFER_OPERATION_SUBJECT, TRANSFER_CHANGE_TEXT)
                                .onErrorResume(ex -> {
                                    log.error("Не удалось отправить уведомление: {}", ex.getMessage());
                                    return Mono.empty();
                                })
                ))
                .then(Mono.defer(() -> transfersRepository.save(operation)))
                .doOnSuccess(saved -> log.info("Перевод с ID {} на ID {} успешно сохранён.",
                        dto.getAccountIdFrom(), dto.getAccountIdTo()))
                .then()
                .onErrorResume(ex -> {
                    log.error("Ошибка перевода с ID {} на ID {}: {}",
                            dto.getAccountIdFrom(), dto.getAccountIdTo(), ex.getMessage());
                    return Mono.error(ex);
                });
    }
}
