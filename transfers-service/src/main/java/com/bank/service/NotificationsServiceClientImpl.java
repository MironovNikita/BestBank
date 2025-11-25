package com.bank.service;

import com.bank.dto.email.EmailNotificationDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsServiceClientImpl implements NotificationsServiceClient {

    private final WebClient notificationsWebClient;
    private final Retry notificationsServiceRetry;
    private final CircuitBreaker notificationsServiceCB;

    public Mono<Void> sendTransferNotification(String email, String subject, String text) {
        EmailNotificationDto emailDto = new EmailNotificationDto(email, subject, text);

        return notificationsWebClient
                .post()
                .uri("/email")
                .bodyValue(emailDto)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("Ошибка при отправке уведомления: {}", email);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    log.info("Уведомление отправлено на {}", email);
                    return Mono.empty();
                })
                .transformDeferred(CircuitBreakerOperator.of(notificationsServiceCB))
                .transformDeferred(RetryOperator.of(notificationsServiceRetry))
                .then();
    }
}
