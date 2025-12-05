package com.bank.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleCurrencyException(CurrencyException e) {
        log.error("Возникло CurrencyException: {}", e.getMessage(), e);

        return Mono.just(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleException(Exception e) {
        log.error("Возникло необработанное исключение: {}", e.getMessage());

        return Mono.just("Произошла непредвиденная ошибка. Попробуйте позднее.");
    }
}
