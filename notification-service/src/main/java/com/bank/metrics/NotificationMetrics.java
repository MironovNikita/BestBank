package com.bank.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_FAILED_NOTIFICATION;
import static com.bank.metrics.MetricConstants.METRIC_SUCCESS_NOTIFICATION;

@Slf4j
@Component
public class NotificationMetrics {

    private final MeterRegistry registry;

    public NotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccessfulNotification(String email) {
        Counter.builder(METRIC_SUCCESS_NOTIFICATION)
                .tag("email", email)
                .tag("service", "notification-service")
                .tag("status", "success")
                .description("Successful user notification")
                .register(registry)
                .increment();

        log.debug("Успешная отправка уведомления пользователю по email {}", email);
    }

    public void recordFailedNotification(String email) {
        Counter.builder(METRIC_FAILED_NOTIFICATION)
                .tag("email", email)
                .tag("service", "notification-service")
                .tag("status", "failure")
                .description("Failed user notification")
                .register(registry)
                .increment();

        log.debug("Неудачная отправка уведомления пользователю по email {}", email);
    }
}
