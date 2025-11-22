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
public class NotificationsServiceClientImpl implements NotificationServiceClient {

    private final WebClient notificationsWebClient;
    private final Retry notificationsServiceRetry;
    private final CircuitBreaker notificationsServiceCB;

    public Mono<Void> sendNotification(String toEmail, String subject, String text) {
        EmailNotificationDto email = new EmailNotificationDto(toEmail, subject, text);

        return notificationsWebClient
                .post()
                .uri("/email")
                .bodyValue(email)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error("Ошибка при отправке уведомления на почту: {}", toEmail);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    log.info("Успешная отправка уведомления на почту: {}", toEmail);
                    return Mono.empty();
                })
                .transformDeferred(CircuitBreakerOperator.of(notificationsServiceCB))
                .transformDeferred(RetryOperator.of(notificationsServiceRetry))
                .then();
    }
}
