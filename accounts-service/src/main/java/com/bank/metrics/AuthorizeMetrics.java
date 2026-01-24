package com.bank.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_FAILED_LOGIN;
import static com.bank.metrics.MetricConstants.METRIC_SUCCESS_LOGIN;

@Slf4j
@Component
public class AuthorizeMetrics {

    private final MeterRegistry registry;

    public AuthorizeMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccessfulLogin(String email) {
        registry.counter(METRIC_SUCCESS_LOGIN,
                        "status", "success",
                        "service", "accounts-service",
                        "email", email)
                .increment();

        log.debug("Успешный логин для пользователя с email: {}", email);
    }

    public void recordFailedLogin(String email) {
        registry.counter(METRIC_FAILED_LOGIN,
                        "status", "failure",
                        "service", "accounts-service",
                        "email", email)
                .increment();

        log.debug("Неудачная попытка логина с использованием email: {}", email);
    }
}
