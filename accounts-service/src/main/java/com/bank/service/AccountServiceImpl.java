package com.bank.service;

import com.bank.common.exception.*;
import com.bank.common.mapper.AccountMapper;
import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
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
import reactor.core.scheduler.Schedulers;

import static com.bank.dto.email.EmailTemplates.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureBase64Converter converter;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Mono<Void> register(RegisterAccountRequest req) {
        Account account = accountMapper.toAccount(req);

        return accountRepository.save(account)
                .flatMap(acc -> {
                    log.info("Успешное создание аккаунта с ID: {}", acc.getId());

                    notificationService.sendNotification(req.getEmail(), REGISTRATION_SUBJECT, REGISTRATION_TEXT.formatted(acc.getName(), acc.getSurname()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(req.getEmail(), ex.getMessage()))
                            .subscribe();

                    return Mono.just(acc);
                })
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

    @Override
    public Flux<AccountMainPageDto> getAllAccounts(Long requestedId) {
        return accountRepository.getAllAccountsForMainPage(requestedId);
    }

    @Override
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

                    String email = converter.decrypt(account.getEmail());
                    notificationService.sendNotification(email, PASSWORD_CHANGE_SUBJECT, PASSWORD_CHANGE_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(email, ex.getMessage()))
                            .subscribe();

                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Пароль для пользователя с ID {} успешно обновлён.", saved.getId()));
                })
                .doOnError(error -> log.error("Ошибка обновления пароля для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    @Override
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
                            .flatMap(updated -> {
                                String email =
                                        (accountUpdateDto.getEmail() != null && !accountUpdateDto.getEmail().isBlank())
                                                ? accountUpdateDto.getEmail()
                                                : converter.decrypt(account.getEmail());
                                notificationService.sendNotification(email, ACCOUNT_CHANGE_SUBJECT, ACCOUNT_CHANGE_TEXT)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnError(ex -> logEmailError(email, ex.getMessage()))
                                        .subscribe();
                                log.info("Данные пользователя с ID {} были успешно обновлены.", updated.getId());
                                return Mono.just(updated);
                            })
                            .onErrorMap(ex -> {
                                log.error("Возникло исключение при обновлении данных: {}", ex.getMessage());
                                return new AccountEditException();
                            });
                })
                .doOnError(error -> log.error("Ошибка обновления данных для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    @Override
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

    @Override
    public Mono<BalanceDto> getBalance(Long accountId) {
        return accountRepository.getAccountBalance(accountId)
                .flatMap(balance -> {
                    log.info("Был запрошен баланс для аккаунта с ID {}", accountId);
                    return Mono.just(new BalanceDto(accountId, balance));
                });
    }

    @Override
    @Transactional
    public Mono<Void> updateBalance(Long accountId, UpdateBalanceRq updateBalanceRq) {
        return accountRepository.updateAccountBalance(accountId, updateBalanceRq.getBalance())
                .doOnSuccess(v -> log.info("Баланс для аккаунта с ID {} был успешно изменён", accountId));
    }

    @Override
    @Transactional
    public Mono<Void> transfer(TransferOperationDto transferOperationDto) {
        return accountRepository.transfer(transferOperationDto)
                .doOnSuccess(v -> log.info("Перевод с аккаунта с ID {} на аккаунт с ID {} успешно совершён.",
                        transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo()));
    }

    private boolean checkField(String field) {
        return field != null && !field.isEmpty();
    }

    private void logEmailError(String email, String exceptionMessage) {
        log.error("Ошибка при отправке уведомления для {}: {}", email, exceptionMessage);
    }
}

