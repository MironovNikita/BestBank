package com.bank.service;

import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.exception.BlockerException;
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
@Transactional
public class TransfersServiceImpl implements TransfersService {

    private final TransfersRepository transfersRepository;
    private final TransferOperationMapper transferOperationMapper;
    private final SecureBase64Converter converter;
    private final AccountsServiceClient accountsServiceClient;
    private final NotificationsServiceClient notificationsServiceClient;
    private final ExchangeServiceClient exchangeServiceClient;
    private final BlockerServiceClient blockerServiceClient;

    @Override
    public Mono<Void> operateTransfer(TransferOperationDto dto) {
        checkAccountsId(dto.getAccountIdFrom(), dto.getAccountIdTo());

        return blockerServiceClient.checkOperation()
                .flatMap(allowed -> {
                    if (!allowed) {
                        log.error("Операция с наличными была заблокирована для счёта с ID {}", dto.getAccountIdFrom());
                        return Mono.error(new BlockerException());
                    }

                    Mono<TransferOperationDto> preparedDto = dto.getCurrencyFrom() != dto.getCurrencyTo()
                            ?
                            exchangeServiceClient.recountTransferAmount(dto)
                                    .map(newAmount -> {
                                        dto.setAmountTo(newAmount);
                                        return dto;
                                    })
                            :
                            Mono.fromCallable(() -> {
                                dto.setAmountTo(dto.getAmountFrom());
                                return dto;
                            });

                    return preparedDto.flatMap(updatedDto -> {
                        TransferOperation operation = transferOperationMapper.toTransferOperation(updatedDto);

                        return accountsServiceClient.transfer(updatedDto)
                                .then(Mono.defer(() -> {
                                            String email = converter.decrypt(dto.getEmail());
                                            return notificationsServiceClient.sendTransferNotification(email, TRANSFER_OPERATION_SUBJECT, TRANSFER_CHANGE_TEXT)
                                                    .onErrorResume(ex -> {
                                                        log.error("Не удалось отправить уведомление: {}", ex.getMessage());
                                                        return Mono.empty();
                                                    });
                                        }
                                ))
                                .then(Mono.defer(() -> transfersRepository.save(operation)))
                                .doOnSuccess(saved -> log.info("Перевод с ID {} на ID {} успешно сохранён.",
                                        dto.getAccountIdFrom(), dto.getAccountIdTo()))
                                .then();
                    });
                })
                .onErrorResume(ex -> {
                    log.error("Ошибка перевода со счёта с ID {} на счёт с ID {}: {}",
                            dto.getAccountIdFrom(), dto.getAccountIdTo(), ex.getMessage());
                    return Mono.error(ex);
                });
    }

    private void checkAccountsId(Long accountIdFrom, Long accountIdTo) {
        if (accountIdFrom.equals(accountIdTo)) throw new IllegalArgumentException("Нельзя перевести средства. Счёт списания идентичен счёту получения");
    }
}
