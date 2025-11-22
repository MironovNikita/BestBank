package com.bank.service;

import com.bank.common.exception.TransferException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsServiceClientImpl implements AccountsServiceClient {

    private final WebClient accountsWebClient;
    private final Retry accountsServiceRetry;
    private final CircuitBreaker accountsServiceCB;

    public Mono<Void> transfer(TransferOperationDto dto) {
        return accountsWebClient
                .post()
                .uri("/accounts/transfer")
                .bodyValue(dto)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("4xx ошибка при переводе средств: {}", msg);
                                    return Mono.error(new TransferException());
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }
                    return resp.releaseBody();
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry));
    }
}
