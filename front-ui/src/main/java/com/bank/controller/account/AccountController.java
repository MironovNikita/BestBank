package com.bank.controller.account;

import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
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

    @PostMapping("/editPassword")
    public Mono<String> editPassword(@ModelAttribute AccountPasswordChangeDto changePass, WebSession session) {

        return checkUserId(session)
                .flatMap(userId -> accountsWebClient
                        .post()
                        .uri("/accounts/{id}/editPassword", userId)
                        .bodyValue(changePass)
                        .exchangeToMono(resp -> {
                            if (resp.statusCode().is4xxClientError()) {
                                return resp.bodyToMono(String.class)
                                        .map(msg -> {
                                            log.error("4хх ошибка при обращении (изменение пароля) к accounts-service: {}", msg);
                                            session.getAttributes().put("accountErrors", List.of(msg));
                                            return "redirect:/main";
                                        });
                            }
                            if (resp.statusCode().is5xxServerError()) {
                                return resp.bodyToMono(String.class)
                                        .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                            }
                            return resp.releaseBody()
                                    .then(Mono.fromCallable(() -> {
                                        session.getAttributes().put("successPasswordMessage", "Пароль был успешно изменён");
                                        return "redirect:/main";
                                    }));
                        })
                        .onErrorResume(ex -> {
                            log.error("5хх ошибка при обращении (изменение пароля) к accounts-service: {}", ex.getMessage());
                            session.getAttributes().put("passwordErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                            return Mono.just("redirect:/main");
                        })
                )
                .onErrorResume(ex -> {
                    log.error("Ошибка при проверке userId (изменение пароля)");
                    session.getAttributes().put("passwordErrors", List.of(ex.getMessage()));
                    return Mono.just("redirect:/main");
                });
    }

    @PostMapping("/editAccount")
    public Mono<String> editAccount(@ModelAttribute AccountUpdateDto accountUpdateDto, WebSession session) {

        return checkUserId(session)
                .flatMap(userId -> accountsWebClient
                        .post()
                        .uri("/accounts/{id}/editAccount", userId)
                        .bodyValue(accountUpdateDto)
                        .exchangeToMono(resp -> {
                            if (resp.statusCode().is4xxClientError()) {
                                return resp.bodyToMono(String.class)
                                        .map(msg -> {
                                            log.error("4хх ошибка при обращении (изменение аккаунта) к accounts-service: {}", msg);
                                            session.getAttributes().put("accountErrors", List.of(msg));
                                            return "redirect:/main";
                                        });
                            }
                            if (resp.statusCode().is5xxServerError()) {
                                return resp.bodyToMono(String.class)
                                        .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                            }
                            return resp.releaseBody()
                                    .then(Mono.fromCallable(() -> {
                                        session.getAttributes().put("successUpdateAccMessage", "Аккаунт был успешно обновлён");
                                        return "redirect:/main";
                                    }));
                        })
                        .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                        .transformDeferred(RetryOperator.of(accountsServiceRetry))
                        .onErrorResume(ex -> {
                            log.error("5хх ошибка при обращении (изменение аккаунта) к accounts-service: {}", ex.getMessage());
                            session.getAttributes().put("accountErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                            return Mono.just("redirect:/main");
                        })
                )
                .onErrorResume(ex -> {
                    log.error("Ошибка при проверке userId (изменение аккаунта)");
                    session.getAttributes().put("accountErrors", List.of(ex.getMessage()));
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
