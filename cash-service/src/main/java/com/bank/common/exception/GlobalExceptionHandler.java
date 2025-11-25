package com.bank.common.exception;

import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(NotEnoughFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleNotEnoughFundsException(NotEnoughFundsException e) {
        log.error("Возникло NotEnoughFundsException: {}", e.getMessage(), e);

        return Mono.just(e.getMessage());
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleException(Exception e) {
        log.error("Возникло необработанное исключение: {}", e.getMessage());
        return Mono.just("Произошла непредвиденная ошибка. Попробуйте позднее.");
    }
}
