package com.bank.metrics;

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
        registry.counter(METRIC_SUCCESS_NOTIFICATION,
                        "email", email,
                        "service", "notification-service",
                        "status", "success")
                .increment();

        log.debug("Успешная отправка уведомления пользователю по email {}", email);
    }

    public void recordFailedNotification(String email) {
        registry.counter(METRIC_FAILED_NOTIFICATION,
                        "email", email,
                        "service", "notification-service",
                        "status", "failure")
                .increment();

        log.debug("Неудачная отправка уведомления пользователю по email {}", email);
    }
}
