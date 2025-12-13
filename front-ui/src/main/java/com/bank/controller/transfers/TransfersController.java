package com.bank.controller.transfers;

import com.bank.dto.transfer.TransferOperationDto;
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
public class TransfersController {

    private final WebClient transfersWebClient;
    private final Retry transfersServiceRetry;
    private final CircuitBreaker transfersServiceCB;

    @PostMapping("/transfer/self")
    public Mono<String> selfTransfer(@ModelAttribute TransferOperationDto dto, WebSession session) {
        dto.setEmail(session.getAttribute("email"));

        return checkUserId(session)
                .flatMap(userId ->
                        transfersWebClient
                                .post()
                                .uri("/transfer")
                                .bodyValue(dto)
                                .exchangeToMono(resp -> {
                                    if (resp.statusCode().is4xxClientError()) {
                                        return resp.bodyToMono(String.class)
                                                .map(msg -> {
                                                    log.error("4хх ошибка при обращении (перевод средств cо счёта на счёт) к accounts-service: {}", msg);
                                                    session.getAttributes().put("selfTransferErrors", List.of(msg));
                                                    return "redirect:/main";
                                                });
                                    }
                                    if (resp.statusCode().is5xxServerError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                                    }
                                    return resp.releaseBody()
                                            .then(Mono.fromCallable(() -> {
                                                session.getAttributes().put("selfTransferSuccess", "Операция перевода успешно выполнена");
                                                return "redirect:/main";
                                            }));
                                })
                                .transformDeferred(CircuitBreakerOperator.of(transfersServiceCB))
                                .transformDeferred(RetryOperator.of(transfersServiceRetry))
                                .onErrorResume(ex -> {
                                    log.error("Произошла ошибка при обращении (перевод средств со счёта на счёт) к transfers-service: {}", ex.getMessage());
                                    session.getAttributes().put("selfTransferErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                                    return Mono.just("redirect:/main");
                                }))
                .onErrorResume(ex -> {
                    session.getAttributes().put("selfTransferErrors", List.of("Ошибка авторизации: " + ex.getMessage()));
                    return Mono.just("redirect:/main");
                });
    }

    @PostMapping("/transfer/other")
    public Mono<String> otherTransfer(@ModelAttribute TransferOperationDto dto, WebSession session) {
        dto.setEmail(session.getAttribute("email"));

        return checkUserId(session)
                .flatMap(userId ->
                        transfersWebClient
                                .post()
                                .uri("/transfer")
                                .bodyValue(dto)
                                .exchangeToMono(resp -> {
                                    if (resp.statusCode().is4xxClientError()) {
                                        return resp.bodyToMono(String.class)
                                                .map(msg -> {
                                                    log.error("4хх ошибка при обращении (перевод средств на счёт другого пользователя) к accounts-service: {}", msg);
                                                    session.getAttributes().put("transferErrors", List.of(msg));
                                                    return "redirect:/main";
                                                });
                                    }
                                    if (resp.statusCode().is5xxServerError()) {
                                        return resp.bodyToMono(String.class)
                                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                                    }
                                    return resp.releaseBody()
                                            .then(Mono.fromCallable(() -> {
                                                session.getAttributes().put("successTransferMessage", "Операция перевода успешно выполнена");
                                                return "redirect:/main";
                                            }));
                                })
                                .transformDeferred(CircuitBreakerOperator.of(transfersServiceCB))
                                .transformDeferred(RetryOperator.of(transfersServiceRetry))
                                .onErrorResume(ex -> {
                                    log.error("Произошла ошибка при обращении (перевод средств на счёт другого пользователя) к transfers-service: {}", ex.getMessage());
                                    session.getAttributes().put("transferErrors", "Произошла неизвестная ошибка. Попробуйте позднее.");
                                    return Mono.just("redirect:/main");
                                }))
                .onErrorResume(ex -> {
                    session.getAttributes().put("transferErrors", List.of("Ошибка авторизации: " + ex.getMessage()));
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
