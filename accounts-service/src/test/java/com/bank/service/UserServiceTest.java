package com.bank.service;

import com.bank.common.exception.*;
import com.bank.common.mapper.UserMapper;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import com.bank.entity.User;
import com.bank.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationsService notificationsService;
    @Mock
    private AccountService accountService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecureBase64Converter converter;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Проверка метода регистрации")
    void shouldRegisterAccount() {
        RegisterUserRequest rq = createRegisterRq();
        Long userId = 1L;
        User user = createUser(userId);

        when(userMapper.toUser(rq)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(Mono.just(user));
        when(notificationsService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.register(rq))
                .verifyComplete();

        verify(userMapper).toUser(rq);
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Проверка ошибки, если указанные данные уже есть")
    void shouldThrowExceptionIfDataAlreadyExists() {
        RegisterUserRequest rq = createRegisterRq();
        Long userId = 1L;
        User user = createUser(userId);

        when(userMapper.toUser(rq)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(Mono.error(new DataIntegrityViolationException("")));

        StepVerifier.create(userService.register(rq))
                .expectError(RegistrationException.class)
                .verify();

        verify(userMapper).toUser(rq);
        verify(userRepository).save(user);
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка успешного логина")
    void shouldSuccessLogin() {
        LoginRequest rq = createLoginRequest();
        User user = createUser(1L);

        when(userRepository.getUserByEmail(rq.getEmail())).thenReturn(Mono.just(user));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        StepVerifier.create(userService.login(rq))
                .expectNextMatches(resp ->
                        resp.getId().equals(1L)
                                && resp.getEmail().equals(rq.getEmail())
                                && resp.getName().equals("Test"))
                .verifyComplete();

        verify(userRepository).getUserByEmail(rq.getEmail());
        verify(converter).encrypt(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка неуспешного логина")
    void shouldFailLogin() {
        LoginRequest rq = createLoginRequest();

        when(userRepository.getUserByEmail(rq.getEmail())).thenReturn(Mono.error(new LoginException()));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");

        StepVerifier.create(userService.login(rq))
                .expectError(LoginException.class)
                .verify();

        verify(userRepository).getUserByEmail(rq.getEmail());
        verify(converter).encrypt(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения пароля пользователя")
    void shouldEditPassword() {
        Long userId = 1L;
        UserPasswordChangeDto dto = createAccountPasswordChangeDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("password");
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationsService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(userRepository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.editPassword(userId, dto))
                .verifyComplete();

        verify(userRepository).findUserById(userId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(anyString());
        verify(converter).decrypt(anyString());
        verify(notificationsService).sendNotification(anyString(), anyString(), anyString());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Проверка изменения пароля пользователя, если введён существующий")
    void shouldThrowExceptionIfPasswordExists() {
        Long userId = 1L;
        UserPasswordChangeDto dto = createAccountPasswordChangeDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        StepVerifier.create(userService.editPassword(userId, dto))
                .expectError(PasswordEditException.class)
                .verify();

        verify(userRepository).findUserById(userId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(converter, never()).decrypt(anyString());
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("Проверка изменения пароля пользователя, если он не найден")
    void shouldThrowExceptionIfAccountNotExists() {
        Long userId = 1L;
        UserPasswordChangeDto dto = createAccountPasswordChangeDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.error(new ObjectNotFoundException("Аккаунт", userId)));

        StepVerifier.create(userService.editPassword(userId, dto))
                .expectError(ObjectNotFoundException.class)
                .verify();

        verify(userRepository).findUserById(userId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(converter, never()).decrypt(anyString());
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("Проверка изменения данных пользователя")
    void shouldChangeAccountData() {
        Long userId = 1L;
        UserUpdateDto dto = createAccountUpdateDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.just(user));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(userRepository.save(user)).thenReturn(Mono.just(user));
        when(notificationsService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.editUser(userId, dto))
                .verifyComplete();

        verify(userRepository).findUserById(userId);
        verify(converter).encrypt(anyString());
        verify(userRepository).save(user);
        verify(notificationsService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта, если email/phone уже существуют")
    void shouldNotChangeAccountDataIfEmailOrPhoneExist() {
        Long userId = 1L;
        UserUpdateDto dto = createAccountUpdateDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.just(user));
        when(converter.encrypt(anyString())).thenReturn("test@test.ru");
        when(userRepository.save(user)).thenReturn(Mono.error(new UserEditException()));

        StepVerifier.create(userService.editUser(userId, dto))
                .expectError(UserEditException.class)
                .verify();

        verify(userRepository).findUserById(userId);
        verify(converter).encrypt(anyString());
        verify(userRepository).save(user);
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта, аккаунт не найден")
    void shouldNotChangeAccountDataIfAccountNotExists() {
        Long userId = 1L;
        UserUpdateDto dto = createAccountUpdateDto();
        User user = createUser(userId);

        when(userRepository.findUserById(userId)).thenReturn(Mono.error(new ObjectNotFoundException("Аккаунт", userId)));

        StepVerifier.create(userService.editUser(userId, dto))
                .expectError(ObjectNotFoundException.class)
                .verify();

        verify(userRepository).findUserById(userId);
        verify(converter, never()).encrypt(anyString());
        verify(userRepository, never()).save(user);
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка успешного удаления пользователя")
    void shouldDeleteUser() {
        Long userId = 1L;
        User user = createUser(userId);

        when(accountService.getUserAccounts(userId)).thenReturn(Flux.empty());
        when(userRepository.deleteById(userId)).thenReturn(Mono.empty());
        when(converter.decrypt(anyString())).thenReturn("test@test.ru");
        when(notificationsService.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(userId, user.getEmail()))
                .verifyComplete();

        verify(accountService).getUserAccounts(userId);
        verify(userRepository).deleteById(userId);
        verify(converter).decrypt(anyString());
        verify(notificationsService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка неудачного удаления пользователя")
    void shouldNotDeleteUserIfAccountsWithPositiveBalance() {
        Long userId = 1L;
        User user = createUser(userId);

        when(accountService.getUserAccounts(userId)).thenReturn(Flux.error(new UserDeleteException("Аккаунт")));

        StepVerifier.create(userService.deleteUser(userId, user.getEmail()))
                .expectError(UserDeleteException.class)
                .verify();

        verify(accountService).getUserAccounts(userId);
        verify(userRepository, never()).deleteById(userId);
        verify(converter, never()).decrypt(anyString());
        verify(notificationsService, never()).sendNotification(anyString(), anyString(), anyString());
    }
}
