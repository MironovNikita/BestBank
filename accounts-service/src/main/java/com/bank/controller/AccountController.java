package com.bank.controller;

import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.login.LoginRequest;
import com.bank.login.LoginResponse;
import com.bank.service.AccountServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    //TODO Заменить на интерфейс
    private final AccountServiceImpl accountService;

    @PostMapping("/register")
    public Mono<Void> register(@RequestBody @Validated RegisterAccountRequest registerRequest) {
        return accountService.register(registerRequest);
    }

    @GetMapping("/{id}")
    public Flux<AccountMainPageDto> getAllAccounts(@PathVariable(name = "id") Long requestedId) {
        return accountService.getAllAccounts(requestedId);
    }

    @PostMapping("/{id}/editPassword")
    public Mono<Void> editPassword(@PathVariable(name = "id") Long id, @Validated @RequestBody AccountPasswordChangeDto passwordChangeDto) {
        return accountService.editPassword(id, passwordChangeDto);
    }

    @PostMapping("/{id}/editAccount")
    public Mono<Void> editAccount(@PathVariable("id") Long id, @Validated @RequestBody AccountUpdateDto accountUpdateDto) {
        return accountService.editAccount(id, accountUpdateDto);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        return accountService.login(loginRequest);
    }

    @GetMapping("/{id}/balance")
    public Mono<BalanceDto> getBalance(@PathVariable(name = "id") Long accountId) {
        return accountService.getBalance(accountId);
    }

    @PostMapping("/{id}/balance")
    public Mono<Void> editBalance(@PathVariable(name = "id") Long accountId, @Validated @RequestBody UpdateBalanceRq updateBalanceRq) {
        return accountService.updateBalance(accountId, updateBalanceRq);
    }
}
