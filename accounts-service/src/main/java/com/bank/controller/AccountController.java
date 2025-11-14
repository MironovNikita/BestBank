package com.bank.controller;

import com.bank.dto.AccountServiceResponse;
import com.bank.entity.AccountMainPageDto;
import com.bank.entity.AccountUpdateDto;
import com.bank.entity.PasswordChangeDto;
import com.bank.entity.RegisterAccountRequest;
import com.bank.service.AccountServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AccountController {

    //TODO Заменить на интерфейс
    private final AccountServiceImpl accountService;

    @PostMapping("/register")
    public Mono<AccountServiceResponse> register(@RequestBody @Validated RegisterAccountRequest registerRequest) {
        return accountService.register(registerRequest);
    }

    @GetMapping("/accounts")
    public Flux<AccountMainPageDto> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/accounts/{id}/editPassword")
    public Mono<AccountServiceResponse> editPassword(@PathVariable("id") Long id, @Validated @RequestBody PasswordChangeDto passwordChangeDto) {
        return accountService.editPassword(id, passwordChangeDto);
    }

    @PostMapping("/accounts/{id}/editAccount")
    public Mono<AccountServiceResponse> editAccount(@PathVariable("id") Long id, @Validated @RequestBody AccountUpdateDto accountUpdateDto) {
        return accountService.editAccount(id, accountUpdateDto);
    }

    //TODO Добавить эндпоинт логина для проверки логина/пароля
}
