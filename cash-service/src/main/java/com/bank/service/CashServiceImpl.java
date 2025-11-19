package com.bank.service;

import com.bank.common.exception.NotEnoughFundsException;
import com.bank.common.mapper.CashOperationMapper;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.CashOperationDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.entity.CashOperation;
import com.bank.repository.CashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static com.bank.entity.OperationType.GET;
import static com.bank.entity.OperationType.PUT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashServiceImpl {

    private final CashRepository cashRepository;
    private final WebClient accountsWebClient;
    private final CashOperationMapper cashOperationMapper;

    @Transactional
    public Mono<Void> operateCash(CashOperationDto cashOperationDto) {
        CashOperation cashOperation = cashOperationMapper.toCashOperation(cashOperationDto);

        return getCurrentBalance(cashOperation.getAccountId())
                .flatMap(balance -> calculateNewBalance(balance, cashOperation))
                .flatMap(newBalance -> updateRemoteBalance(newBalance, cashOperationDto.getAccountId())
                        .then(saveOperation(cashOperation)))
                .doOnSuccess(v -> log.info("Операция с наличными для пользователя {} выполнена.", cashOperation.getAccountId()))
                .then();
    }

    private Mono<Long> getCurrentBalance(Long accountId) {
        return accountsWebClient
                .get()
                .uri("/accounts/{id}/balance", accountId)
                .retrieve()
                .bodyToMono(BalanceDto.class)
                .map(BalanceDto::getBalance)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("Ошибка получения баланса для пользователя с ID {}: {}", accountId, ex.getMessage());
                    return new RuntimeException("Ошибка получения баланса: " + ex.getResponseBodyAsString(), ex);
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
                .toBodilessEntity()
                .then();
    }

    private Mono<Void> saveOperation(CashOperation cashOperation) {
        return cashRepository.save(cashOperation).then();
    }
}
