package com.bank.controller;

import com.bank.dto.account.*;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.transfer.TransferOperationDto;
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

    @GetMapping("/{id}/balance")
    public Mono<BalanceDto> getBalance(@PathVariable(name = "id") Long accountId) {
        return accountService.getAccountBalance(accountId);
    }

    @PostMapping("/{id}/balance")
    public Mono<Void> editBalance(@PathVariable(name = "id") Long accountId, @Validated @RequestBody UpdateBalanceRq updateBalanceRq) {
        return accountService.updateBalance(accountId, updateBalanceRq);
    }

    @PostMapping("/transfer")
    public Mono<Void> transfer(@Validated @RequestBody TransferOperationDto transferOperationDto) {
        return accountService.transfer(transferOperationDto);
    }

    @GetMapping("/{id}")
    public Flux<AccountOtherListDto> getAllOtherAccounts(@PathVariable(name = "id") Long userRequestedId) {
        return accountService.getAllOtherAccounts(userRequestedId);
    }
}
