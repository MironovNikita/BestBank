package com.bank.service;

import com.bank.common.exception.NotEnoughFundsException;
import com.bank.common.mapper.CashOperationMapper;
import com.bank.dto.cash.CashOperationDto;
import com.bank.entity.CashOperation;
import com.bank.repository.CashRepository;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final CashOperationMapper cashOperationMapper;
    private final SecureBase64Converter converter;
    private final AccountsServiceClient accountsServiceClient;
    private final NotificationsServiceClient notificationsServiceClient;


    @Override
    @Transactional
    public Mono<Void> operateCash(CashOperationDto cashOperationDto) {
        CashOperation cashOperation = cashOperationMapper.toCashOperation(cashOperationDto);

        return accountsServiceClient.getCurrentBalance(cashOperation.getAccountId())
                .flatMap(balance -> calculateNewBalance(balance, cashOperation))
                .flatMap(newBalance -> accountsServiceClient.updateRemoteBalance(newBalance, cashOperationDto.getAccountId())
                        .then(saveOperation(cashOperation)))
                .doOnSuccess(v -> {
                    log.info("Операция с наличными для пользователя {} выполнена.", cashOperation.getAccountId());

                    String email = converter.decrypt(cashOperationDto.getEmail());
                    notificationsServiceClient.sendNotification(email, CASH_OPERATION_SUBJECT, CASH_OPERATION_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> log.error("Ошибка при отправке уведомления для {}: {}", email, ex.getMessage()))
                            .subscribe();
                })
                .then();
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

    private Mono<Void> saveOperation(CashOperation cashOperation) {
        return cashRepository.save(cashOperation).then();
    }
}
