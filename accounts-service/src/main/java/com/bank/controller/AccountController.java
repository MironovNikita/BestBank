package com.bank.controller;

import com.bank.dto.account.AccountCreateDto;
import com.bank.dto.account.AccountDeleteDto;
import com.bank.dto.account.AccountEditDto;
import com.bank.dto.account.AccountListDto;
import com.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/currencies/{id}")
    public Flux<AccountListDto> getUserCurrencies(@PathVariable(name = "id") Long userId) {
        return accountService.getUserAccounts(userId);
    }

    @PostMapping("/create/{id}")
    public Mono<Void> createAccount(@Validated @RequestBody AccountCreateDto dto, @PathVariable(name = "id") Long userId) {
        return accountService.createAccount(dto, userId);
    }

    @PostMapping("/delete")
    public Mono<Void> deleteAccount(@Validated @RequestBody AccountDeleteDto dto) {
        return accountService.deleteAccount(dto);
    }

    @PostMapping("/edit")
    public Mono<Void> editAccount(@Validated @RequestBody AccountEditDto dto) {
        return accountService.editAccount(dto);
    }
/*
    //TODO Переделать
    @GetMapping("/{id}")
    public Flux<AccountListDto> getAllAccounts(@PathVariable(name = "id") Long requestedId) {
        return accountService.getAllAccounts(requestedId);
    }

    //TODO Переделать
    @GetMapping("/{id}/balance")
    public Mono<BalanceDto> getBalance(@PathVariable(name = "id") Long accountId) {
        return accountService.getBalance(accountId);
    }

    //TODO Переделать
    @PostMapping("/{id}/balance")
    public Mono<Void> editBalance(@PathVariable(name = "id") Long accountId, @Validated @RequestBody UpdateBalanceRq updateBalanceRq) {
        return accountService.updateBalance(accountId, updateBalanceRq);
    }

    //TODO Переделать
    @PostMapping("/transfer")
    public Mono<Void> transfer(@Validated @RequestBody TransferOperationDto transferOperationDto) {
        return accountService.transfer(transferOperationDto);
    }*/
}
