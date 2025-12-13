package com.bank.controller.cash;

import com.bank.dto.cash.CashOperationDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CashController {

    private final WebClient cashWebClient;
    private final Retry cashServiceRetry;
    private final CircuitBreaker cashServiceCB;

    @PostMapping("/cash")
    public Mono<String> operateCash(@ModelAttribute CashOperationDto cashDto, WebSession session) {
        cashDto.setEmail(session.getAttribute("email"));

        return checkUserId(session)
                .flatMap(userId ->
                        cashWebClient
                                .post()
                                .uri("/cash")
                                .bodyValue(cashDto)
                                .exchangeToMono(resp -> {
                                    if (resp.statusCode().is4xxClientError()) {
                                        return resp.bodyToMono(String.class)
                                                .map(msg -> {
                                                    log.error("4хх ошибка при обращении (операции с наличными) к cash-service: {}", msg);
                                                    session.getAttributes().put("cashErrors", List.of(msg));
                                                    return "redirect:/main";
                                                });
                                    }
                                    if (resp.statusCode().is5xxServerError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                                    }
                                    return resp.releaseBody()
                                            .then(Mono.fromCallable(() -> {
                                                session.getAttributes().put("successCashMessage", "Операция успешно выполнена");
                                                return "redirect:/main";
                                            }));
                                })
                                .transformDeferred(CircuitBreakerOperator.of(cashServiceCB))
                                .transformDeferred(RetryOperator.of(cashServiceRetry))
                                .onErrorResume(ex -> {
                                    log.error("Произошла ошибка при обращении (операции с наличными) к cash-service: {}", ex.getMessage());
                                    session.getAttributes().put("cashErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                                    return Mono.just("redirect:/main");
                                }))
                .onErrorResume(ex -> {
                    log.error("Ошибка авторизации: {}", ex.getMessage());
                    session.getAttributes().put("cashErrors", List.of("Ошибка авторизации: " + ex.getMessage()));
                    return Mono.just("redirect:/main");
                });
    }

    private Mono<Long> checkUserId(WebSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (!(userIdObj instanceof Number)) {
            return Mono.error(new RuntimeException("Пользователь не авторизован, либо в сессии указан некорректный ID."));
        }
        return Mono.just(((Number) userIdObj).longValue());
    }
}
