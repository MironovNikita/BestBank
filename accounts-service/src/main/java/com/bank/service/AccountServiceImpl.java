package com.bank.service;

import com.bank.common.exception.AccountOperationException;
import com.bank.common.mapper.AccountMapper;
import com.bank.dto.account.*;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
import com.bank.repository.AccountRepository;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

import static com.bank.dto.email.EmailTemplates.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final NotificationsService notificationsService;
    private final SecureBase64Converter converter;

    public Flux<AccountListDto> getUserAccounts(Long id) {
        return accountRepository.getAllUserAccountsById(id);
    }

    @Override
    @Transactional
    public Mono<Void> createAccount(AccountCreateDto dto, Long userId) {
        Account account = accountMapper.toAccount(dto, userId);

        return accountRepository.save(account)
                .flatMap(acc -> {
                    log.info("Успешное создание счёта с ID: {}", acc.getId());

                    String email = converter.decrypt(dto.getEmail());
                    notificationsService.sendNotification(email, ACCOUNT_CREATION_SUBJECT, ACCOUNT_CREATION_TEXT.formatted(dto.getCurrency()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(email, ex.getMessage()))
                            .subscribe();

                    return Mono.just(acc);
                })
                .onErrorResume(ex -> {
                    log.error("При создании счёта в валюте {} для пользователя с ID {} возникла ошибка: {}", dto.getCurrency(), userId, ex.getMessage());
                    return Mono.error(ex);
                })
                .then();
    }

    @Override
    @Transactional
    public Mono<Void> deleteAccount(AccountDeleteDto dto) {
        return accountRepository.getAccountBalance(dto.getId())
                .flatMap(balance -> {
                    if (balance.compareTo(BigDecimal.ZERO) > 0) return Mono.error(new AccountOperationException("Ошибка удаления счёта. Баланс положительный"));

                    return accountRepository.deleteById(dto.getId())
                            .doOnSuccess(v -> {
                                log.info("Успешное удаление счёта с ID: {}", dto.getId());

                                String email = converter.decrypt(dto.getEmail());
                                notificationsService.sendNotification(email, ACCOUNT_DELETION_SUBJECT, ACCOUNT_DELETION_TEXT.formatted(dto.getCurrency()))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnError(ex -> logEmailError(email, ex.getMessage()))
                                        .subscribe();
                            })
                            .onErrorResume(ex -> {
                                log.error("При удалении счёта в валюте {} возникла ошибка: {}", dto.getCurrency(), ex.getMessage());
                                return Mono.error(ex);
                            })
                            .then();
                });
    }

    @Override
    @Transactional
    public Mono<Void> editAccount(AccountEditDto dto) {
        return accountRepository.editAccountTitleById(dto.getId(), dto.getNewTitle())
                .flatMap(acc -> {
                    log.info("Успешное обновление названия счёта с ID: {}", dto.getId());

                    String email = converter.decrypt(dto.getEmail());
                    notificationsService.sendNotification(email, ACCOUNT_UPDATE_SUBJECT, ACCOUNT_UPDATE_TEXT.formatted(dto.getCurrency()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(email, ex.getMessage()))
                            .subscribe();

                    return Mono.just(acc);
                })
                .onErrorResume(ex -> {
                    log.error("При обновлении счёта в валюте {} возникла ошибка: {}", dto.getCurrency(), ex.getMessage());
                    return Mono.error(ex);
                })
                .then();
    }

    @Override
    public Mono<BalanceDto> getAccountBalance(Long accountId) {
        return accountRepository.getAccountBalance(accountId)
                .flatMap(balance -> {
                    log.info("Был запрошен баланс для счёта с ID {}", accountId);
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
                .doOnSuccess(v -> log.info("Перевод со счёта с ID {} на счёт с ID {} успешно совершён.",
                        transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo()));
    }

    @Override
    public Flux<AccountOtherListDto> getAllOtherAccounts(Long requestedId) {
        return accountRepository.getAllAccountsForMainPage(requestedId);
    }

    private void logEmailError(String email, String exceptionMessage) {
        log.error("Ошибка при отправке уведомления для {}: {}", email, exceptionMessage);
    }
}

