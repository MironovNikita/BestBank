package com.bank.controller.main;

import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.security.CustomUserDetails;
import com.bank.security.SecureBase64Converter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final WebClient accountsWebClient;
    private final SecureBase64Converter converter;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;
    private final Retry accountsServiceRetry;
    private final CircuitBreaker accountsServiceCB;

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
                .exchangeToMono(resp -> {

                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .map(msg -> {
                                    log.error("4хх ошибка при обращении (регистрация) к accounts-service: {}", msg);
                                    model.addAttribute("errors", List.of(msg));
                                    return "register";
                                });
                    }

                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }

                    return resp.releaseBody()
                            .then(Mono.fromCallable(() -> {
                                model.addAttribute("successMessage", "Поздравляем с успешной регистрацией! Можете войти!");
                                return "login";
                            }));
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                .onErrorResume(ex -> {
                    log.error("5хх ошибка при обращении к accounts-service (register): {}", ex.getMessage());
                    var errors = model.getAttribute("errors");
                    if (errors != null) model.addAttribute("errors", List.of("Произошла неизвестная ошибка. Попробуйте позднее."));
                    return Mono.just("register");
                });
    }

    @GetMapping("/login")
    public Mono<String> loginPage() {
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
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        return resp.bodyToMono(String.class)
                                .map(msg -> {
                                    log.error("4хх ошибка при обращении (логин) к accounts-service: {}", msg);
                                    model.addAttribute("errors", List.of(msg));
                                    return "login";
                                });
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(msg)));
                    }

                    return resp.bodyToMono(LoginResponse.class)
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
                                                                session.getAttributes().put("email", converter.encrypt(loginRequest.getEmail()));
                                                            })
                                                            .thenReturn("redirect:/main")));

                                        });
                            });
                })
                .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                .transformDeferred(RetryOperator.of(accountsServiceRetry))
                .onErrorResume(ex -> {
                    log.error("5хх ошибка при обращении к accounts-service (login): {}", ex.getMessage());
                    var errors = model.getAttribute("errors");
                    if (errors != null) model.addAttribute("errors", List.of("Произошла неизвестная ошибка. Попробуйте позднее."));
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
                .flatMap(userId -> {
                    Mono<List<AccountMainPageDto>> accountsMono = accountsWebClient
                            .get()
                            .uri("/accounts/{id}", userId)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(body))))
                            .bodyToFlux(AccountMainPageDto.class)
                            .collectList()
                            .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                            .transformDeferred(RetryOperator.of(accountsServiceRetry))
                            .onErrorResume(ex -> {
                                log.error("Не удалось получить список аккаунтов: {}", ex.getMessage());
                                return Mono.just(List.of());
                            });

                    Mono<Long> balanceMono = accountsWebClient
                            .get()
                            .uri("/accounts/{id}/balance", userId)
                            .retrieve()
                            .bodyToMono(BalanceDto.class)
                            .map(BalanceDto::getBalance)
                            .transformDeferred(CircuitBreakerOperator.of(accountsServiceCB))
                            .transformDeferred(RetryOperator.of(accountsServiceRetry))
                            .onErrorResume(ex -> {
                                log.error("Не удалось получить баланс: {}", ex.getMessage());
                                return Mono.just(Long.MIN_VALUE);
                            });

                    return Mono.zip(accountsMono, balanceMono)
                            .flatMap(tuple -> {
                                model.addAttribute("accounts", tuple.getT1());
                                model.addAttribute("userName", session.getAttribute("userName"));
                                var balance = tuple.getT2();
                                model.addAttribute("balance", balance == Long.MIN_VALUE ? "Сервер временно недоступен. Попробуйте позже" : balance);
                                return Mono.just("main");
                            });
                })
                .onErrorResume(ex -> {
                    log.error("Ошибка авторизации: {}", ex.getMessage());
                    session.getAttributes().put("errors", List.of("Пользователь не авторизован, либо в сессии указан некорректный ID."));
                    return Mono.just("main");
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
        model.addAttribute("successCashMessage", session.getAttribute("successCashMessage"));
        model.addAttribute("cashErrors", session.getAttribute("cashErrors"));
        model.addAttribute("successTransferMessage", session.getAttribute("successTransferMessage"));
        model.addAttribute("transferErrors", session.getAttribute("transferErrors"));

        session.getAttributes().remove("successPasswordMessage");
        session.getAttributes().remove("passwordErrors");
        session.getAttributes().remove("successUpdateAccMessage");
        session.getAttributes().remove("accountErrors");
        session.getAttributes().remove("successCashMessage");
        session.getAttributes().remove("cashErrors");
        session.getAttributes().remove("successTransferMessage");
        session.getAttributes().remove("transferErrors");
    }
}
