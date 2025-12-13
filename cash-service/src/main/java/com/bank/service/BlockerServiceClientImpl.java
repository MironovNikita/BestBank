package com.bank.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockerServiceClientImpl implements BlockerServiceClient {

    private final WebClient blockerWebClient;
    private final Retry blockerServiceRetry;
    private final CircuitBreaker blockerServiceCB;

    @Override
    public Mono<Boolean> checkOperation() {
        return blockerWebClient
                .get()
                .uri("/blocker/check")
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("4хх ошибка при обращении (запрос баланса) к accounts-service: {}", msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }
                    return resp.bodyToMono(Boolean.class);
                })
                .transformDeferred(CircuitBreakerOperator.of(blockerServiceCB))
                .transformDeferred(RetryOperator.of(blockerServiceRetry))
                .onErrorMap(ex -> {
                    log.error("Ошибка проверки операции: {}", ex.getMessage());
                    return new RuntimeException("Ошибка проверки операции: " + ex.getMessage(), ex);
                });
    }
}
