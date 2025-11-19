package com.bank.controller.transfers;

import com.bank.dto.transfer.TransferOperationDto;
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
public class TransfersController {

    private final WebClient transfersWebClient;

    @PostMapping("/transfer")
    public Mono<String> transfer(ServerWebExchange exchange, WebSession session) {
        return checkUserId(session)
                .flatMap(userId ->
                        exchange.getFormData()
                                .flatMap(form -> {
                                    String transferValue = form.getFirst("transferValue");
                                    String accountIdTo = form.getFirst("accountTo");

                                    if (transferValue == null || accountIdTo == null) {
                                        session.getAttributes().put("transferErrors", List.of("Поля формы перевода заполнены неверно!"));
                                        return Mono.just("redirect:/main");
                                    }

                                    Long amount;
                                    Long accountTo;
                                    try {
                                        accountTo = Long.valueOf(accountIdTo);
                                        amount = Long.valueOf(transferValue);
                                    } catch (NumberFormatException e) {
                                        session.getAttributes().put("transferErrors", List.of("Некорректная сумма!"));
                                        return Mono.just("redirect:/main");
                                    }

                                    TransferOperationDto transferOperationDto = new TransferOperationDto(userId, accountTo, session.getAttribute("email"), amount);

                                    return transfersWebClient
                                            .post()
                                            .uri("/transfer")
                                            .bodyValue(transferOperationDto)
                                            .retrieve()
                                            .onStatus(HttpStatusCode::isError,
                                                    resp -> resp.bodyToMono(String.class)
                                                            .flatMap(body -> Mono.error(new RuntimeException(body))))
                                            .toBodilessEntity()
                                            .map(r -> {
                                                session.getAttributes().put("successTransferMessage", "Операция перевода успешно выполнена");
                                                return "redirect:/main";
                                            })
                                            .onErrorResume(ex -> {
                                                session.getAttributes().put("transferErrors", List.of(ex.getMessage()));
                                                return Mono.just("redirect:/main");
                                            });
                                })
                )
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
