package com.bank.controller.account;

import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final WebClient accountsWebClient;

    @PostMapping("/editPassword")
    public Mono<String> editPassword(@ModelAttribute AccountPasswordChangeDto changePass, WebSession session) {

        return checkUserId(session)
                .flatMap(userId -> accountsWebClient
                        .post()
                        .uri("/accounts/{id}/editPassword", userId)
                        .bodyValue(changePass)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(body))))
                        .toBodilessEntity()
                        .map(response -> {
                            session.getAttributes().put("successPasswordMessage", "Пароль был успешно изменён");
                            return "redirect:/main";
                        })
                        .onErrorResume(WebClientResponseException.class, ex -> {
                            session.getAttributes().put("passwordErrors", List.of(ex.getResponseBodyAsString()));
                            return Mono.just("redirect:/main");
                        })
                        .onErrorResume(Exception.class, ex -> {
                            session.getAttributes().put("passwordErrors", List.of(ex.getMessage()));
                            return Mono.just("redirect:/main");
                        })
                )
                .onErrorResume(ex -> {
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
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(body))))
                        .toBodilessEntity()
                        .map(response -> {
                            session.getAttributes().put("successUpdateAccMessage", "Аккаунт был успешно обновлён");
                            return "redirect:/main";
                        })
                        .onErrorResume(WebClientResponseException.class, ex -> {
                            session.getAttributes().put("accountErrors", List.of(ex.getResponseBodyAsString()));
                            return Mono.just("redirect:/main");
                        })
                        .onErrorResume(Exception.class, ex -> {
                            session.getAttributes().put("accountErrors", List.of(ex.getMessage()));
                            return Mono.just("redirect:/main");
                        })
                )
                .onErrorResume(ex -> {
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
