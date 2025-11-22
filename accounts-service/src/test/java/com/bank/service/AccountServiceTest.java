package com.bank.service;

import com.bank.common.exception.*;
import com.bank.common.mapper.AccountMapper;
import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.login.LoginRequest;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    @DisplayName("Проверка метода регистрации")
    void shouldRegisterAccount() {
        RegisterAccountRequest rq = createRegisterRq();
        Long accountId = 1L;
        Account account = createAccount(accountId);

        when(accountMapper.toAccount(rq)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(Mono.just(account));
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.register(rq))
                .verifyComplete();

        verify(accountMapper).toAccount(rq);
        verify(accountRepository).save(account);
        verifyNoMoreInteractions(accountMapper);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Проверка ошибки, если указанные данные уже есть")
    void shouldThrowExceptionIfDataAlreadyExists() {
        RegisterAccountRequest rq = createRegisterRq();
        Long accountId = 1L;
        Account account = createAccount(accountId);

        when(accountMapper.toAccount(rq)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(Mono.error(new DataIntegrityViolationException("")));

        StepVerifier.create(accountService.register(rq))
                .expectError(RegistrationException.class)
                .verify();

        verify(accountMapper).toAccount(rq);
        verify(accountRepository).save(account);
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка получения всех доступных аккаунтов")
    void shouldReturnAllAccountsForUserMainPage() {
        Long accountId = 1L;

        AccountMainPageDto dto1 = createAccountMainPageDto("2");
        AccountMainPageDto dto2 = createAccountMainPageDto("3");

        when(accountRepository.getAllAccountsForMainPage(accountId)).thenReturn(Flux.just(dto1, dto2));

        StepVerifier.create(accountService.getAllAccounts(accountId))
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();

        verify(accountRepository).getAllAccountsForMainPage(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Проверка изменения пароля аккаунта")
    void shouldEditPassword() {
        Long accountId = 1L;
        AccountPasswordChangeDto dto = createAccountPasswordChangeDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.just(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("password");
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(accountRepository.save(account)).thenReturn(Mono.just(account));

        StepVerifier.create(accountService.editPassword(accountId, dto))
                .verifyComplete();

        verify(accountRepository).findAccountById(accountId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(anyString());
        verify(converter).decrypt(anyString());
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Проверка изменения пароля аккаунта, если введён существующий")
    void shouldThrowExceptionIfPasswordExists() {
        Long accountId = 1L;
        AccountPasswordChangeDto dto = createAccountPasswordChangeDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.just(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        StepVerifier.create(accountService.editPassword(accountId, dto))
                .expectError(PasswordEditException.class)
                .verify();

        verify(accountRepository).findAccountById(accountId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(converter, never()).decrypt(anyString());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
        verify(accountRepository, never()).save(account);
    }

    @Test
    @DisplayName("Проверка изменения пароля аккаунта, если аккаунт не найден")
    void shouldThrowExceptionIfAccountNotExists() {
        Long accountId = 1L;
        AccountPasswordChangeDto dto = createAccountPasswordChangeDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.error(new ObjectNotFoundException("Аккаунт", accountId)));

        StepVerifier.create(accountService.editPassword(accountId, dto))
                .expectError(ObjectNotFoundException.class)
                .verify();

        verify(accountRepository).findAccountById(accountId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(converter, never()).decrypt(anyString());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
        verify(accountRepository, never()).save(account);
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта")
    void shouldChangeAccountData() {
        Long accountId = 1L;
        AccountUpdateDto dto = createAccountUpdateDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.just(account));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(accountRepository.save(account)).thenReturn(Mono.just(account));
        when(notificationService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.editAccount(accountId, dto))
                .verifyComplete();

        verify(accountRepository).findAccountById(accountId);
        verify(converter).encrypt(anyString());
        verify(accountRepository).save(account);
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта, если email/phone уже существуют")
    void shouldNotChangeAccountDataIfEmailOrPhoneExist() {
        Long accountId = 1L;
        AccountUpdateDto dto = createAccountUpdateDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.just(account));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(accountRepository.save(account)).thenReturn(Mono.error(new AccountEditException()));

        StepVerifier.create(accountService.editAccount(accountId, dto))
                .expectError(AccountEditException.class)
                .verify();

        verify(accountRepository).findAccountById(accountId);
        verify(converter).encrypt(anyString());
        verify(accountRepository).save(account);
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта, аккаунт не найден")
    void shouldNotChangeAccountDataIfAccountNotExists() {
        Long accountId = 1L;
        AccountUpdateDto dto = createAccountUpdateDto();
        Account account = createAccount(accountId);

        when(accountRepository.findAccountById(accountId)).thenReturn(Mono.error(new ObjectNotFoundException("Аккаунт", accountId)));

        StepVerifier.create(accountService.editAccount(accountId, dto))
                .expectError(ObjectNotFoundException.class)
                .verify();

        verify(accountRepository).findAccountById(accountId);
        verify(converter, never()).encrypt(anyString());
        verify(accountRepository, never()).save(account);
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка успешного логина")
    void shouldSuccessLogin() {
        LoginRequest rq = createLoginRequest();
        Account account = createAccount(1L);

        when(accountRepository.getAccountByEmail(rq.getEmail())).thenReturn(Mono.just(account));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        StepVerifier.create(accountService.login(rq))
                .expectNextMatches(resp ->
                        resp.getId().equals(1L)
                                && resp.getEmail().equals(rq.getEmail())
                                && resp.getName().equals("test"))
                .verifyComplete();

        verify(accountRepository).getAccountByEmail(rq.getEmail());
        verify(converter).encrypt(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка неуспешного логина")
    void shouldFailLogin() {
        LoginRequest rq = createLoginRequest();

        when(accountRepository.getAccountByEmail(rq.getEmail())).thenReturn(Mono.error(new LoginException()));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");

        StepVerifier.create(accountService.login(rq))
                .expectError(LoginException.class)
                .verify();

        verify(accountRepository).getAccountByEmail(rq.getEmail());
        verify(converter).encrypt(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка получения баланса")
    void shouldGetBalanceById() {
        Long accountId = 1L;
        Long balance = 1000L;
        BalanceDto dto = new BalanceDto(accountId, balance);

        when(accountRepository.getAccountBalance(accountId)).thenReturn(Mono.just(balance));

        StepVerifier.create(accountService.getBalance(accountId))
                .expectNext(dto)
                .verifyComplete();

        verify(accountRepository).getAccountBalance(accountId);
    }

    @Test
    @DisplayName("Проверка обновления баланса")
    void shouldUpdateBalance() {
        TransferOperationDto dto = createTransferOperationDto(1L, 2L);

        when(accountRepository.transfer(dto)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.transfer(dto))
                .verifyComplete();

        verify(accountRepository).transfer(dto);
    }

    @Test
    @DisplayName("Проверка ошибки во время обновления баланса")
    void shouldNotUpdateBalanceIfException() {
        TransferOperationDto dto = createTransferOperationDto(1L, 2L);

        when(accountRepository.transfer(dto)).thenReturn(Mono.error(new TransferException()));

        StepVerifier.create(accountService.transfer(dto))
                .expectError(TransferException.class)
                .verify();

        verify(accountRepository).transfer(dto);
    }
}
