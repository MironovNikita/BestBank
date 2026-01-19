package com.bank.service;

import com.bank.common.exception.*;
import com.bank.common.mapper.UserMapper;
import com.bank.dto.account.AccountListDto;
import com.bank.dto.user.UserUpdateDto;
import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.entity.User;
import com.bank.metrics.AuthorizeMetrics;
import com.bank.repository.UserRepository;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.bank.dto.email.EmailTemplates.*;
import static com.bank.dto.email.EmailTemplates.PASSWORD_CHANGE_TEXT;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final NotificationsService notificationsService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final SecureBase64Converter converter;
    private final AuthorizeMetrics authorizeMetrics;

    @Override
    @Transactional
    public Mono<Void> register(RegisterUserRequest req) {
        User user = userMapper.toUser(req);

        return userRepository.save(user)
                .flatMap(usr -> {
                    log.info("Успешное создание пользователя с ID: {}", usr.getId());

                    notificationsService.sendNotification(req.getEmail(), REGISTRATION_SUBJECT, REGISTRATION_TEXT.formatted(usr.getName(), usr.getSurname()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(req.getEmail(), ex.getMessage()))
                            .subscribe();

                    return Mono.just(usr);
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
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        String email = converter.encrypt(loginRequest.getEmail().toLowerCase());
        return userRepository.getUserByEmail(email)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка входа. Пользователь с email {} не найден.", loginRequest.getEmail());
                    return Mono.error(new LoginException());
                }))
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        log.info("Успешная проверка credentials для пользователя с email {}", loginRequest.getEmail());
                        authorizeMetrics.recordSuccessfulLogin(email);
                        return Mono.just(new LoginResponse().setId(user.getId()).setEmail(user.getEmail()).setName(user.getName()));
                    } else {
                        log.error("Ошибка входа. Неверный пароль для email: {}", loginRequest.getEmail());
                        authorizeMetrics.recordFailedLogin(email);
                        return Mono.error(new LoginException());
                    }
                });
    }

    @Override
    @Transactional
    public Mono<Void> editPassword(Long id, UserPasswordChangeDto passwordChangeDto) {
        String newPassword = passwordChangeDto.getNewPassword();
        String confirmPassword = passwordChangeDto.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            log.error("Введённые пароли не совпадают.");
            return Mono.error(new PasswordEditException());
        }

        return userRepository.findUserById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка изменения пароля. Пользователь с id {} не был найден.", id);
                    return Mono.error(new ObjectNotFoundException("Аккаунт", id));
                }))
                .flatMap(user -> {
                    if (passwordEncoder.matches(newPassword, user.getPassword())) return Mono.error(new PasswordEditException());
                    user.setPassword(passwordEncoder.encode(newPassword));

                    String email = converter.decrypt(user.getEmail());
                    notificationsService.sendNotification(email, PASSWORD_CHANGE_SUBJECT, PASSWORD_CHANGE_TEXT)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(ex -> logEmailError(email, ex.getMessage()))
                            .subscribe();

                    return userRepository.save(user)
                            .doOnSuccess(saved -> log.info("Пароль для пользователя с ID {} успешно обновлён.", saved.getId()));
                })
                .doOnError(error -> log.error("Ошибка обновления пароля для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    @Override
    @Transactional
    public Mono<Void> editUser(Long id, UserUpdateDto userUpdateDto) {

        return userRepository.findUserById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка изменения данных пользователя. Пользователь с id {} не был найден.", id);
                    return Mono.error(new ObjectNotFoundException("Пользователь", id));
                }))
                .flatMap(account -> {
                    if (checkField(userUpdateDto.getEmail())) account.setEmail(converter.encrypt(userUpdateDto.getEmail()));
                    if (checkField(userUpdateDto.getName())) account.setName(userUpdateDto.getName());
                    if (checkField(userUpdateDto.getSurname())) account.setSurname(userUpdateDto.getSurname());
                    if (checkField(userUpdateDto.getPhone())) account.setPhone(userUpdateDto.getPhone());
                    if (userUpdateDto.getBirthdate() != null) account.setBirthdate(userUpdateDto.getBirthdate());

                    return userRepository.save(account)
                            .flatMap(updated -> {
                                String email =
                                        (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank())
                                                ? userUpdateDto.getEmail()
                                                : converter.decrypt(account.getEmail());
                                notificationsService.sendNotification(email, ACCOUNT_CHANGE_SUBJECT, ACCOUNT_CHANGE_TEXT)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnError(ex -> logEmailError(email, ex.getMessage()))
                                        .subscribe();
                                log.info("Данные пользователя с ID {} были успешно обновлены.", updated.getId());
                                return Mono.just(updated);
                            })
                            .onErrorMap(ex -> {
                                log.error("Возникло исключение при обновлении данных: {}", ex.getMessage());
                                return new UserEditException();
                            });
                })
                .doOnError(error -> log.error("Ошибка обновления данных для пользователя с ID {}: {}", id, error.getMessage()))
                .then();
    }

    @Override
    @Transactional
    public Mono<Void> deleteUser(Long userId, String email) {

        return accountService.getUserAccounts(userId)
                .collectList()
                .flatMap(accounts -> {
                    for (AccountListDto account : accounts) {
                        if (account.getBalance().longValue() > 0) {
                            log.warn("Пользователь с ID {} не был удалён. Есть положительный баланс на одном из счетов: {}", userId, account.getCurrency());
                            return Mono.error(new UserDeleteException("У вас есть счёт (%s) с положительным балансом. Удаление личного кабинета невозможно".formatted(account.getCurrency())));
                        }
                    }

                    return userRepository.deleteById(userId)
                            .doOnSuccess(v -> {
                                log.info("Пользователь с ID {} был успешно удалён.", userId);

                                String userEmail = converter.decrypt(email);
                                notificationsService.sendNotification(userEmail, ACCOUNT_CHANGE_SUBJECT, ACCOUNT_CHANGE_TEXT)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnError(ex -> logEmailError(userEmail, ex.getMessage()))
                                        .subscribe();
                            })
                            .onErrorMap(ex -> new ObjectNotFoundException("Пользователь", userId));
                });
    }

    private boolean checkField(String field) {
        return field != null && !field.isEmpty();
    }

    private void logEmailError(String email, String exceptionMessage) {
        log.error("Ошибка при отправке уведомления для {}: {}", email, exceptionMessage);
    }

}
