package com.bank.service;

import com.bank.dto.account.AccountCreateDto;
import com.bank.dto.account.AccountDeleteDto;
import com.bank.dto.account.AccountEditDto;
import com.bank.dto.account.AccountListDto;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<AccountListDto> getUserAccounts(Long id);

    Mono<Void> createAccount(AccountCreateDto accountCreateDto, Long userId);

    Mono<Void> deleteAccount(AccountDeleteDto dto);

    Mono<Void> editAccount(AccountEditDto dto);

    Mono<BalanceDto> getAccountBalance(Long accountId);

    Mono<Void> updateBalance(Long accountId, UpdateBalanceRq updateBalanceRq);
}
