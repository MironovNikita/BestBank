package com.bank.service;

import com.bank.dto.account.AccountCreateDto;
import com.bank.dto.account.AccountDeleteDto;
import com.bank.dto.account.AccountListDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<AccountListDto> getUserAccounts(Long id);

    Mono<Void> createAccount(AccountCreateDto accountCreateDto, Long userId);

    Mono<Void> deleteAccount(AccountDeleteDto dto);
}
