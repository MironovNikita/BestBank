package com.bank.service;

import com.bank.dto.email.EmailNotificationDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {

    private final Retry notificationsServiceRetry;
    private final CircuitBreaker notificationsServiceCB;

    private final KafkaTemplate<String, EmailNotificationDto> kafkaTemplate;

    @Value("${spring.kafka.topic.notification}")
    private String notificationTopic;

    @Override
    public Mono<Void> sendNotification(String toEmail, String subject, String text) {
        EmailNotificationDto email = new EmailNotificationDto(toEmail, subject, text);

        return Mono.fromFuture(() -> kafkaTemplate.send(notificationTopic, email).toCompletableFuture())
                .transformDeferred(CircuitBreakerOperator.of(notificationsServiceCB))
                .transformDeferred(RetryOperator.of(notificationsServiceRetry))
                .doOnSuccess(result -> log.info("Уведомление успешно отправлено на email: {}, topic: {}, offset: {}",
                        email,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset()))
                .doOnError(e -> log.error("Ошибка отправки уведомления на {}: {}", toEmail, e.getMessage()))
                .then();
    }
}
