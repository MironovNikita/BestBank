package com.bank.service;

import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
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


    public Mono<Long> getCurrentBalance(Long accountId) {
        return accountsWebClient
                .get()
                .uri("/accounts/{id}/balance", accountId)
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
                    return resp.bodyToMono(BalanceDto.class)
                            .map(BalanceDto::getBalance);
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                .onErrorMap(ex -> {
                    log.error("Ошибка получения баланса для пользователя с ID {}: {}", accountId, ex.getMessage());
                    return new RuntimeException("Ошибка получения баланса: " + ex.getMessage(), ex);
                });
    }

    public Mono<Void> updateRemoteBalance(Long newBalance, Long accountId) {
        UpdateBalanceRq updateBalanceRq = new UpdateBalanceRq(newBalance);

        return accountsWebClient
                .post()
                .uri("/accounts/{id}/balance", accountId)
                .bodyValue(updateBalanceRq)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(msg -> {
                            log.error("Ошибка при обращении (обновление баланса) к accounts-service: {}", msg);
                            return Mono.error(new RuntimeException(msg));
                        }))
                .toBodilessEntity()
                .then()
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry));
    }
}
