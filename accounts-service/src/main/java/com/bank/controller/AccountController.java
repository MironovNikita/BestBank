package com.bank.controller;

import com.bank.dto.AccountMainPageDto;
import com.bank.dto.AccountPasswordChangeDto;
import com.bank.dto.AccountUpdateDto;
import com.bank.dto.RegisterAccountRequest;
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
}
