package com.bank.controller.account;

import com.bank.dto.account.AccountCreateDto;
import com.bank.dto.account.AccountDeleteDto;
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
public class AccountController {

    private final WebClient accountsWebClient;
    private final Retry accountsServiceRetry;
    private final CircuitBreaker accountsServiceCB;

    @PostMapping("/accounts/create")
    public Mono<String> createAccount(@ModelAttribute AccountCreateDto createRq, WebSession session) {
        createRq.setEmail(session.getAttribute("email"));

        return checkUserId(session)
                .flatMap(userId ->
                        accountsWebClient
                                .post()
                                .uri("/accounts/create/{id}", userId)
                                .bodyValue(createRq)
                                .exchangeToMono(resp -> {

                                    if (resp.statusCode().is4xxClientError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> {
                                                    log.error("4хх ошибка при обращении (создание счёта) к accounts-service: {}", msg);
                                                    session.getAttributes().put("accountListErrors", List.of(msg));
                                                    return Mono.just("redirect:/main");
                                                });
                                    }
                                    if (resp.statusCode().is5xxServerError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                                    }
                                    return resp.releaseBody()
                                            .then(Mono.fromCallable(() -> {
                                                session.getAttributes().put("accountListSuccess", "Счёт успешно создан");
                                                return "redirect:/main";
                                            }));
                                })
                                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                                .onErrorResume(ex -> {
                                    log.error("Произошла ошибка при обращении (создание счёта) к accounts-service: {}", ex.getMessage());
                                    session.getAttributes().put("accountListErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                                    return Mono.just("redirect:/main");
                                }))
                .onErrorResume(ex -> {
                    log.error("Ошибка авторизации: {}", ex.getMessage());
                    session.getAttributes().put("accountListErrors", List.of("Ошибка авторизации: " + ex.getMessage()));
                    return Mono.just("redirect:/main");
                });
    }

    @PostMapping("/accounts/delete")
    public Mono<String> deleteAccount(@ModelAttribute AccountDeleteDto dto, WebSession session) {
        dto.setEmail(session.getAttribute("email"));

        return checkUserId(session)
                .flatMap(userId ->
                        accountsWebClient
                                .post()
                                .uri("/accounts/delete")
                                .bodyValue(dto)
                                .exchangeToMono(resp -> {

                                    if (resp.statusCode().is4xxClientError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> {
                                                    log.error("4хх ошибка при обращении (удаление счёта) к accounts-service: {}", msg);
                                                    session.getAttributes().put("accountListErrors", List.of(msg));
                                                    return Mono.just("redirect:/main");
                                                });
                                    }
                                    if (resp.statusCode().is5xxServerError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                                    }

                                    return resp.releaseBody()
                                            .then(Mono.fromCallable(() -> {
                                                session.getAttributes().put("accountListSuccess", "Счёт успешно удалён");
                                                return "redirect:/main";
                                            }));
                                })
                                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                                .onErrorResume(ex -> {
                                    log.error("Произошла ошибка при обращении (удаление счёта) к accounts-service: {}", ex.getMessage());
                                    session.getAttributes().put("accountListErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                                    return Mono.just("redirect:/main");
                                }))
                .onErrorResume(ex -> {
                    log.error("Ошибка авторизации : {}", ex.getMessage());
                    session.getAttributes().put("accountListErrors", List.of("Ошибка авторизации: " + ex.getMessage()));
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
