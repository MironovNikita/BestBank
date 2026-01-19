package com.bank.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_FAILED_LOGIN;
import static com.bank.metrics.MetricConstants.METRIC_SUCCESS_LOGIN;

@Slf4j
@Component
public class AuthorizeMetrics {

    private final MeterRegistry registry;

    public AuthorizeMetrics(MeterRegistry meterRegistry) {
        this.registry = meterRegistry;
    }

    public void recordSuccessfulLogin(String email) {
        Counter.builder(METRIC_SUCCESS_LOGIN)
                .tag("status", "success")
                .tag("service", "accounts-service")
                .tag("email", email)
                .description("Successful user login attempt")
                .register(registry)
                .increment();

        log.debug("Успешный логин для пользователя с email: {}", email);
    }

    public void recordFailedLogin(String email) {
        Counter.builder(METRIC_FAILED_LOGIN)
                .tag("status", "failure")
                .tag("service", "accounts-service")
                .tag("email", email)
                .description("Failed user login attempt")
                .register(registry)
                .increment();

        log.debug("Неудачная попытка логина с использованием email: {}", email);
    }
}
