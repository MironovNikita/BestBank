package com.bank.controller;

import com.bank.dto.AccountMainPageDto;
import com.bank.dto.AccountPasswordChangeDto;
import com.bank.dto.AccountUpdateDto;
import com.bank.dto.RegisterAccountRequest;
import com.bank.login.LoginRequest;
import com.bank.login.LoginResponse;
import com.bank.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final WebClient accountsWebClient;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    @Value("${spring.webflux.base-path}")
    private String basePath;

    @Value("${spring.cloud.gateway.discovery.routes[0].uri}")
    private String accountServiceUrl;

    @GetMapping("/register")
    public Mono<String> registerPage() {
        return Mono.just("register");
    }

    @PostMapping("/register")
    public Mono<String> register(@ModelAttribute RegisterAccountRequest registerRequest, Model model) {
        return accountsWebClient
                .post()
                .uri("/accounts/register")
                .bodyValue(registerRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException(body))))
                .toBodilessEntity()
                .map(response -> {
                    model.addAttribute("successMessage", "Поздравляем с успешной регистрацией! Можете войти!");
                    return "login";
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    model.addAttribute("errors", List.of(ex.getResponseBodyAsString()));
                    return Mono.just("register");
                })
                .onErrorResume(Exception.class, ex -> {
                    model.addAttribute("errors", List.of(ex.getMessage()));
                    return Mono.just("register");
                });
    }

    @GetMapping("/login")
    public Mono<String> loginPage() {
        //model.addAttribute("accountServiceUrl", accountServiceUrl);

        return Mono.just("login");
    }

    @PostMapping("/login")
    public Mono<String> login(@ModelAttribute LoginRequest loginRequest,
                              ServerWebExchange exchange,
                              Model model) {

        return accountsWebClient
                .post()
                .uri("/accounts/login")
                .bodyValue(loginRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException(body))))
                .bodyToMono(LoginResponse.class)
                .flatMap(loginResponse -> {

                    CustomUserDetails principal = new CustomUserDetails(loginResponse.getId(), loginResponse.getEmail(), loginResponse.getName());
                    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    return authenticationManager.authenticate(auth)
                            .flatMap(authentication -> {
                                SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

                                return securityContextRepository.save(exchange, securityContext)
                                        .then(Mono.defer(() -> exchange.getSession()
                                                .doOnNext(session -> {
                                                    session.getAttributes().put("userName", loginResponse.getName());
                                                    session.getAttributes().put("userId", loginResponse.getId());
                                                })
                                                .thenReturn("redirect:/main")));

                            });
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    model.addAttribute("errors", List.of(ex.getResponseBodyAsString()));
                    return Mono.just("login");
                })
                .onErrorResume(Exception.class, ex -> {
                    model.addAttribute("errors", List.of(ex.getMessage()));
                    return Mono.just("login");
                });
    }

    @GetMapping("/")
    public Mono<String> mainRedirect() {
        return Mono.just("main");
    }

    @GetMapping("/main")
    public Mono<String> mainPage(WebSession session, Model model) {
        handleMainPage(session, model);

        return checkUserId(session)
                .flatMap(userId ->
                        accountsWebClient
                                .get()
                                .uri("/accounts/{id}", userId)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new RuntimeException(body))))
                                .bodyToFlux(AccountMainPageDto.class)
                                .collectList()
                                .flatMap(accountList -> {
                                    model.addAttribute("accounts", accountList);
                                    model.addAttribute("userName", session.getAttribute("userName"));
                                    return Mono.just("main");
                                })
                );
    }

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

    private void handleMainPage(WebSession session, Model model) {
        model.addAttribute("successPasswordMessage", session.getAttribute("successPasswordMessage"));
        model.addAttribute("passwordErrors", session.getAttribute("passwordErrors"));
        model.addAttribute("successUpdateAccMessage", session.getAttribute("successUpdateAccMessage"));
        model.addAttribute("accountErrors", session.getAttribute("accountErrors"));

        session.getAttributes().remove("successPasswordMessage");
        session.getAttributes().remove("passwordErrors");
        session.getAttributes().remove("successUpdateAccMessage");
        session.getAttributes().remove("accountErrors");
    }
}
