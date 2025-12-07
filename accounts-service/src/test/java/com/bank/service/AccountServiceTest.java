package com.bank.service;

import com.bank.common.exception.AccountOperationException;
import com.bank.common.exception.TransferException;
import com.bank.common.mapper.AccountMapper;
import com.bank.dto.account.*;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.currency.Currency;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
import com.bank.repository.AccountRepository;
import com.bank.security.SecureBase64Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.bank.DataCreator.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecureBase64Converter converter;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountServiceImpl accountService;


    @Test
    @DisplayName("Проверка получения всех счетов пользователя")
    void shouldGetAllUserAccounts() {
        Long userId = 1L;
        AccountListDto dto1 = createAccountListDto(1L, userId);
        AccountListDto dto2 = createAccountListDto(2L, userId);

        when(accountRepository.getAllUserAccountsById(1L)).thenReturn(Flux.just(dto1, dto2));

        StepVerifier.create(accountService.getUserAccounts(userId))
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();

        verify(accountRepository).getAllUserAccountsById(userId);
    }

    @Test
    @DisplayName("Проверка создания счёта")
    void shouldCreateAccount() {
        Long userId = 1L;
        AccountCreateDto dto = createAccountCreateDto();
        Account account = createAccount(1L, userId, Currency.RUB);

        when(accountMapper.toAccount(dto, userId)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(Mono.just(account));
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.createAccount(dto, userId))
                .verifyComplete();

        verify(accountMapper).toAccount(dto, userId);
        verify(accountRepository).save(account);
        verify(converter).decrypt(anyString());
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка удаления счёта")
    void shouldDeleteAccount() {
        Long accountId = 1L;
        AccountDeleteDto dto = createAccountDeleteDto(accountId);

        when(accountRepository.getAccountBalance(accountId)).thenReturn(Mono.just(BigDecimal.valueOf(0)));
        when(accountRepository.deleteById(accountId)).thenReturn(Mono.empty());
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.deleteAccount(dto))
                .verifyComplete();

        verify(accountRepository).getAccountBalance(accountId);
        verify(accountRepository).deleteById(accountId);
        verify(converter).decrypt(anyString());
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка неуспешного удаления счёта, если баланс положительный")
    void shouldNotDeleteAccountIfBalanceIsPositive() {
        Long accountId = 1L;
        AccountDeleteDto dto = createAccountDeleteDto(accountId);

        when(accountRepository.getAccountBalance(accountId)).thenReturn(Mono.error(new AccountOperationException("")));

        StepVerifier.create(accountService.deleteAccount(dto))
                .expectError(AccountOperationException.class)
                .verify();

        verify(accountRepository).getAccountBalance(accountId);
        verify(accountRepository, never()).deleteById(accountId);
        verify(converter, never()).decrypt(anyString());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения названия счёта")
    void shouldUpdateAccountTitle() {
        Long accountId = 1L;
        AccountEditDto dto = createAccountEditDto(accountId);

        when(accountRepository.editAccountTitleById(accountId, dto.getNewTitle())).thenReturn(Mono.just(1));
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.editAccount(dto))
                .verifyComplete();

        verify(accountRepository).editAccountTitleById(accountId, dto.getNewTitle());
        verify(converter).decrypt(anyString());
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка получения баланса")
    void shouldGetBalanceById() {
        Long accountId = 1L;
        BigDecimal balance = BigDecimal.valueOf(1000);
        BalanceDto dto = new BalanceDto(accountId, balance);

        when(accountRepository.getAccountBalance(accountId)).thenReturn(Mono.just(balance));

        StepVerifier.create(accountService.getAccountBalance(accountId))
                .expectNext(dto)
                .verifyComplete();

        verify(accountRepository).getAccountBalance(accountId);
    }

    @Test
    @DisplayName("Проверка обновления баланса")
    void shouldUpdateBalance() {
        Long accountId = 1L;
        UpdateBalanceRq dto = createUpdateBalanceRq();

        when(accountRepository.updateAccountBalance(accountId, dto.getBalance())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.updateBalance(accountId, dto))
                .verifyComplete();

        verify(accountRepository).updateAccountBalance(accountId, dto.getBalance());
    }

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldTransfer() {
        TransferOperationDto dto = createTransferOperationDto(1L, 2L);

        when(accountRepository.transfer(dto)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.transfer(dto))
                .verifyComplete();

        verify(accountRepository).transfer(dto);
    }

    @Test
    @DisplayName("Проверка ошибки во время осуществления перевода")
    void shouldNotTransferIfException() {
        TransferOperationDto dto = createTransferOperationDto(1L, 2L);

        when(accountRepository.transfer(dto)).thenReturn(Mono.error(new TransferException()));

        StepVerifier.create(accountService.transfer(dto))
                .expectError(TransferException.class)
                .verify();

        verify(accountRepository).transfer(dto);
    }

    @Test
    @DisplayName("Проверка получения всех доступных аккаунтов")
    void shouldReturnAllOtherAccountsForUserMainPage() {
        Long accountId = 1L;
        AccountOtherListDto dto1 = createAccountOtherListDto(1L, 2L);
        AccountOtherListDto dto2 = createAccountOtherListDto(2L, 2L);

        when(accountRepository.getAllAccountsForMainPage(accountId)).thenReturn(Flux.just(dto1, dto2));

        StepVerifier.create(accountService.getAllOtherAccounts(accountId))
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();

        verify(accountRepository).getAllAccountsForMainPage(accountId);
        verifyNoMoreInteractions(accountRepository);
    }
}
