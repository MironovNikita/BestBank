package com.bank.controller.cash;

import com.bank.dto.cash.CashOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CashController {

    private final WebClient cashWebClient;

    @PostMapping("/cash")
    public Mono<String> operateCash(ServerWebExchange exchange, WebSession session) {

        return checkUserId(session)
                .flatMap(userId ->
                        exchange.getFormData()
                                .flatMap(form -> {

                                    String amountStr = form.getFirst("amount");
                                    String action = form.getFirst("action");

                                    if (amountStr == null || action == null) {
                                        session.getAttributes().put("cashErrors", List.of("Поля формы заполнены некорректно!"));
                                        return Mono.just("redirect:/main");
                                    }

                                    Long amount;
                                    try {
                                        amount = Long.valueOf(amountStr);
                                    } catch (NumberFormatException e) {
                                        session.getAttributes().put("cashErrors", List.of("Некорректная сумма!"));
                                        return Mono.just("redirect:/main");
                                    }

                                    CashOperationDto cashOperationDto = new CashOperationDto(userId, action, session.getAttribute("email"), amount);

                                    return cashWebClient
                                            .post()
                                            .uri("/cash")
                                            .bodyValue(cashOperationDto)
                                            .retrieve()
                                            .onStatus(HttpStatusCode::isError,
                                                    resp -> resp.bodyToMono(String.class)
                                                            .flatMap(body -> Mono.error(new RuntimeException(body))))
                                            .toBodilessEntity()
                                            .map(r -> {
                                                session.getAttributes().put("successCashMessage", "Операция успешно выполнена");
                                                return "redirect:/main";
                                            })
                                            .onErrorResume(ex -> {
                                                session.getAttributes().put("cashErrors", List.of(ex.getMessage()));
                                                return Mono.just("redirect:/main");
                                            });
                                })
                )
                .onErrorResume(ex -> {
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
