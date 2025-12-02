package com.bank.service;

import com.bank.dto.currency.Currency;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceClientImpl {

    //TODO Интерфейс!

    private final WebClient exchangeServiceWebClient;
    private final Retry exchangeServiceRetry;
    private final CircuitBreaker exchangeServiceCB;

    public Mono<Void> updateExchange(Map<Currency, BigDecimal> rates) {

        return exchangeServiceWebClient
                .post()
                .uri("/exchange/update")
                .bodyValue(rates)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("Ошибка при обновлении курсов валют: {}", msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    log.info("Успешно обновлены новые курсы валют: {}", rates);
                    return Mono.empty();
                })
                .transformDeferred(CircuitBreakerOperator.of(exchangeServiceCB))
                .transformDeferred(RetryOperator.of(exchangeServiceRetry))
                .then();
    }
}
