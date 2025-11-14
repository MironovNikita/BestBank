package com.bank.service;

import com.bank.common.exception.ObjectNotFoundException;
import com.bank.common.exception.PasswordEditException;
import com.bank.common.exception.RegistrationException;
import com.bank.common.mapper.AccountMapper;
import com.bank.common.security.SecureBase64Converter;
import com.bank.dto.AccountServiceResponse;
import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl {

    //TODO ЛОГИ

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureBase64Converter converter;

    @Transactional
    public Mono<AccountServiceResponse> register(RegisterAccountRequest req) {
        Account account = accountMapper.toAccount(req);

        return accountRepository.save(account)
                .map(acc -> {
                    log.info("Пользователь с email {} был успешно зарегистрирован (ID: {})", req.getEmail(), acc.getId());
                    return new AccountServiceResponse(
                            HttpStatus.CREATED.value(),
                            true,
                            List.of()
                    );
                })
                .onErrorResume(ex -> {
                    if (ex instanceof DataIntegrityViolationException) {
                        log.error("При регистрации пользователя с email {} указаны уже существующие параметры: {}", req.getEmail(), ex.getMessage());
                        return Mono.error(new RegistrationException(req.getEmail(), ex.getMessage()));
                    }
                    log.error("При регистрации пользователя с email {} возникла ошибка: {}", req.getEmail(), ex.getMessage());
                    return Mono.error(ex);
                });
    }

    public Flux<AccountMainPageDto> getAllAccounts() {
        return accountRepository.getAllAccountsForMainPage();
    }

    @Transactional
    public Mono<AccountServiceResponse> editPassword(Long id, PasswordChangeDto passwordChangeDto) {
        String newPassword = passwordChangeDto.getNewPassword();
        String confirmPassword = passwordChangeDto.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            log.error("Введённые пароли не совпадают.");
            return Mono.error(new PasswordEditException());
        }

        return accountRepository.findAccountById(id)
                .flatMap(account -> {
                    account.setPassword(passwordEncoder.encode(newPassword));
                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Пароль для пользователя с ID {} успешно обновлён.", saved.getId()))
                            .thenReturn(new AccountServiceResponse(HttpStatus.OK.value(), true, List.of()));
                })
                .doOnError(error -> log.error("Ошибка обновления пароля для пользователя с ID {}: {}", id, error.getMessage()))
                .switchIfEmpty(Mono.error(new ObjectNotFoundException("Аккаунт", id)));
    }

    @Transactional
    public Mono<AccountServiceResponse> editAccount(Long id, AccountUpdateDto accountUpdateDto) {

        return accountRepository.findAccountById(id)
                .flatMap(account -> {
                    if (checkField(accountUpdateDto.getEmail())) account.setEmail(converter.encrypt(accountUpdateDto.getEmail()));
                    if (checkField(accountUpdateDto.getName())) account.setName(accountUpdateDto.getName());
                    if (checkField(accountUpdateDto.getSurname())) account.setSurname(accountUpdateDto.getSurname());
                    if (checkField(accountUpdateDto.getPhone())) account.setPhone(accountUpdateDto.getPhone());
                    if (accountUpdateDto.getBirthdate() != null) account.setBirthdate(accountUpdateDto.getBirthdate());
                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Данные пользователя с ID {} были успешно обновлены.", saved.getId()))
                            .thenReturn(new AccountServiceResponse(HttpStatus.OK.value(), true, List.of()));
                })
                .doOnError(error -> log.error("Ошибка обновления данных для пользователя с ID {}: {}", id, error.getMessage()))
                .switchIfEmpty(Mono.error(new ObjectNotFoundException("Аккаунт", id)));
    }

    private boolean checkField(String field) {
        return field != null && !field.isEmpty();
    }
}

