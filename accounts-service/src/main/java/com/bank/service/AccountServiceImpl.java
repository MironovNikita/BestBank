package com.bank.service;

import com.bank.common.exception.LoginException;
import com.bank.common.exception.ObjectNotFoundException;
import com.bank.common.exception.PasswordEditException;
import com.bank.common.exception.RegistrationException;
import com.bank.common.mapper.AccountMapper;
import com.bank.dto.AccountMainPageDto;
import com.bank.dto.AccountPasswordChangeDto;
import com.bank.dto.AccountUpdateDto;
import com.bank.dto.RegisterAccountRequest;
import com.bank.entity.Account;
import com.bank.login.LoginRequest;
import com.bank.login.LoginResponse;
import com.bank.repository.AccountRepository;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<Void> register(RegisterAccountRequest req) {
        Account account = accountMapper.toAccount(req);

        return accountRepository.save(account)
                .doOnSuccess(acc -> log.info("Успешное создание аккаунта с ID: {}", acc.getId()))
                .onErrorResume(ex -> {
                    if (ex instanceof DataIntegrityViolationException) {
                        log.error("При регистрации пользователя с email {} указаны уже существующие параметры: {}", req.getEmail(), ex.getMessage());
                        return Mono.error(new RegistrationException(req.getEmail(), ex.getMessage()));
                    }
                    log.error("При регистрации пользователя с email {} возникла ошибка: {}", req.getEmail(), ex.getMessage());
                    return Mono.error(ex);
                })
                .then();
    }

    public Flux<AccountMainPageDto> getAllAccounts(Long requestedId) {
        return accountRepository.getAllAccountsForMainPage(requestedId);
    }

    @Transactional
    public Mono<Void> editPassword(Long id, AccountPasswordChangeDto passwordChangeDto) {
        String newPassword = passwordChangeDto.getNewPassword();
        String confirmPassword = passwordChangeDto.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            log.error("Введённые пароли не совпадают.");
            return Mono.error(new PasswordEditException());
        }

        return accountRepository.findAccountById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка изменения пароля. Пользователь с id {} не был найден.", id);
                    return Mono.error(new ObjectNotFoundException("Аккаунт", id));
                }))
                .flatMap(account -> {
                    if (passwordEncoder.matches(newPassword, account.getPassword())) return Mono.error(new PasswordEditException());
                    account.setPassword(passwordEncoder.encode(newPassword));
                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Пароль для пользователя с ID {} успешно обновлён.", saved.getId()));
                })
                .doOnError(error -> log.error("Ошибка обновления пароля для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    @Transactional
    public Mono<Void> editAccount(Long id, AccountUpdateDto accountUpdateDto) {

        return accountRepository.findAccountById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка изменения данных аккаунта. Пользователь с id {} не был найден.", id);
                    return Mono.error(new ObjectNotFoundException("Аккаунт", id));
                }))
                .flatMap(account -> {
                    if (checkField(accountUpdateDto.getEmail())) account.setEmail(converter.encrypt(accountUpdateDto.getEmail()));
                    if (checkField(accountUpdateDto.getName())) account.setName(accountUpdateDto.getName());
                    if (checkField(accountUpdateDto.getSurname())) account.setSurname(accountUpdateDto.getSurname());
                    if (checkField(accountUpdateDto.getPhone())) account.setPhone(accountUpdateDto.getPhone());
                    if (accountUpdateDto.getBirthdate() != null) account.setBirthdate(accountUpdateDto.getBirthdate());
                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Данные пользователя с ID {} были успешно обновлены.", saved.getId()));
                })
                .doOnError(error -> log.error("Ошибка обновления данных для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return accountRepository.getAccountByEmail(converter.encrypt(loginRequest.getEmail().toLowerCase()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка входа. Пользователь с email {} не найден.", loginRequest.getEmail());
                    return Mono.error(new LoginException());
                }))
                .flatMap(account -> {
                    if (passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
                        log.info("Успешная проверка credentials для пользователя с email {}", loginRequest.getEmail());
                        return Mono.just(new LoginResponse().setId(account.getId()).setEmail(account.getEmail()).setName(account.getName()));
                    } else {
                        log.error("Ошибка входа. Неверный пароль для email: {}", loginRequest.getEmail());
                        return Mono.error(new LoginException());
                    }
                });
    }

    private boolean checkField(String field) {
        return field != null && !field.isEmpty();
    }
}

