package com.bank.service;

import com.bank.dto.account.*;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.transfer.TransferOperationDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<AccountListDto> getUserAccounts(Long id);

    Mono<Void> createAccount(AccountCreateDto accountCreateDto, Long userId);

    Mono<Void> deleteAccount(AccountDeleteDto dto);

    Mono<Void> editAccount(AccountEditDto dto);

    Mono<BalanceDto> getAccountBalance(Long accountId);

    Mono<Void> updateBalance(Long accountId, UpdateBalanceRq updateBalanceRq);

    Mono<Void> transfer(TransferOperationDto transferOperationDto);

    Flux<AccountOtherListDto> getAllOtherAccounts(Long requestedId);
}
