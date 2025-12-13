package com.bank.service;

import com.bank.dto.currency.ExchangeCountDto;
import com.bank.dto.transfer.TransferOperationDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceClientImpl implements ExchangeServiceClient {

    private final WebClient exchangeWebClient;
    private final Retry exchangeServiceRetry;
    private final CircuitBreaker exchangeServiceCB;

    @Override
    public Mono<BigDecimal> recountTransferAmount(TransferOperationDto dto) {

        ExchangeCountDto countDto = new ExchangeCountDto(dto.getAmountFrom(), dto.getCurrencyFrom(), dto.getCurrencyTo());

        return exchangeWebClient
                .post()
                .uri("/exchange/recount")
                .bodyValue(countDto)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("4хх ошибка при обращении (конвертация суммы перевода) к exchange-service: {}", msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }

                    return resp.bodyToMono(BigDecimal.class);
                })
                .transformDeferred(CircuitBreakerOperator.of(exchangeServiceCB))
                .transformDeferred(RetryOperator.of(exchangeServiceRetry))
                .onErrorMap(ex -> {
                    log.error("Ошибка конвертации суммы из {} в {}: {}", dto.getCurrencyFrom(), dto.getCurrencyTo(), ex.getMessage());
                    return new RuntimeException("Ошибка конвертации суммы: " + ex.getMessage(), ex);
                });
    }
}
