package com.bank.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleObjectNotFoundException(ObjectNotFoundException e) {
        log.error("Возникло ObjectNotFoundException: {}", e.getMessage(), e);

        return Mono.just(e.getMessage());
    }

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleLoginException(LoginException e) {
        log.error("Возникло LoginException: {}", e.getMessage(), e);

        return Mono.just(e.getMessage());
    }

    @ExceptionHandler({DuplicateKeyException.class, RegistrationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<String> handleDataIntegrityViolationException(Exception e) {
        log.error("Возникло DuplicateKeyException/RegistrationException: {}", e.getMessage());
        var errorMessage = e.getMessage();
        String field = null;
        if (errorMessage != null) {
            if (errorMessage.contains("accounts_email_key")) field = "email";
            else if (errorMessage.contains("accounts_phone_key")) field = "номер телефона";
        }
        String userMessage = field != null
                ? "Ошибка указанных данных. Указанный вами " + field + " уже существует!"
                : "Ошибка указанных данных. Указанные вами параметры уже существуют!";
        return Mono.just(userMessage);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleConstraintViolationException(WebExchangeBindException e) {
        log.error("Возникло ConstraintViolationException: {}", e.getMessage(), e);
        String fieldErrors = e.getFieldErrors()
                .stream()
                .map(fe -> {
                    String errorMessage = fe.getDefaultMessage();
                    return fe.getField() + ":" + (errorMessage != null ? errorMessage : "Недопустимое значение");
                })
                .collect(Collectors.joining("\n"));

        return Mono.just(fieldErrors);
    }

    @ExceptionHandler(PasswordEditException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleObjectNotFoundException(PasswordEditException e) {
        log.error("Возникло PasswordEditException: {}", e.getMessage(), e);

        return Mono.just(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleException(Exception e) {
        log.error("Возникло необработанное исключение: {}", e.getMessage());

        return Mono.just("Произошла непредвиденная ошибка. Попробуйте позднее.");
    }
}
