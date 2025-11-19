package com.bank.service;

import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<Void> register(RegisterAccountRequest req);

    Flux<AccountMainPageDto> getAllAccounts(Long requestedId);

    Mono<Void> editPassword(Long id, AccountPasswordChangeDto passwordChangeDto);

    Mono<Void> editAccount(Long id, AccountUpdateDto accountUpdateDto);

    Mono<LoginResponse> login(LoginRequest loginRequest);

    Mono<BalanceDto> getBalance(Long accountId);

    Mono<Void> updateBalance(Long accountId, UpdateBalanceRq updateBalanceRq);

    Mono<Void> transfer(TransferOperationDto transferOperationDto);
}
