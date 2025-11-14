package com.bank.common.exception;

import com.bank.dto.AccountServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<AccountServiceResponse> handleObjectNotFoundException(ObjectNotFoundException e) {
        log.error("Возникло ObjectNotFoundException: {}", e.getMessage(), e);

        return Mono.just(new AccountServiceResponse(
                HttpStatus.NOT_FOUND.value(),
                false,
                List.of(e.getMessage())
        ));
    }

    @ExceptionHandler({DuplicateKeyException.class, RegistrationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<AccountServiceResponse> handleDataIntegrityViolationException(Exception e) {
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
        return Mono.just(new AccountServiceResponse(
                HttpStatus.CONFLICT.value(),
                false,
                List.of(userMessage)));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<AccountServiceResponse> handleConstraintViolationException(WebExchangeBindException e) {
        log.error("Возникло ConstraintViolationException: {}", e.getMessage(), e);
        List<String> fieldErrors = e.getFieldErrors()
                .stream()
                .map(fe -> {
                    String errorMessage = fe.getDefaultMessage();
                    return fe.getField() + ":" + (errorMessage != null ? errorMessage : "Недопустимое значение");
                })
                .toList();

        return Mono.just(new AccountServiceResponse(
                HttpStatus.BAD_REQUEST.value(),
                false,
                fieldErrors
        ));
    }

    @ExceptionHandler(PasswordEditException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<AccountServiceResponse> handleObjectNotFoundException(PasswordEditException e) {
        log.error("Возникло PasswordEditException: {}", e.getMessage(), e);

        return Mono.just(new AccountServiceResponse(
                HttpStatus.BAD_REQUEST.value(),
                false,
                List.of(e.getMessage())
        ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<AccountServiceResponse> handleException(Exception e) {
        log.error("Возникло необработанное исключение: {}", e.getMessage());

        return Mono.just(new AccountServiceResponse(
                HttpStatus.CONFLICT.value(),
                false,
                List.of("Произошла непредвиденная ошибка. Попробуйте позднее.")));
    }
}
