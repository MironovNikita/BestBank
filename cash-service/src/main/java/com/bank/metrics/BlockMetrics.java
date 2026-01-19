package com.bank.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_ALLOWED_CASH_OPERATION;
import static com.bank.metrics.MetricConstants.METRIC_BLOCKED_CASH_OPERATION;

@Slf4j
@Component
public class BlockMetrics {

    private final MeterRegistry registry;

    public BlockMetrics(MeterRegistry meterRegistry) {
        this.registry = meterRegistry;
    }

    public void recordAllowedOperation(Long accountId, String ownerEmail, String operation) {
        Counter.builder(METRIC_ALLOWED_CASH_OPERATION)
                .tag("account_id", String.valueOf(accountId))
                .tag("owner_email", ownerEmail)
                .tag("operation", operation)
                .tag("service", "cash-service")
                .tag("status", "success")
                .description("Allowed cash operation")
                .register(registry)
                .increment();

        log.debug("Успешная операция с наличными для пользователя с email {}", ownerEmail);
    }

    public void recordBlockedOperation(Long accountId, String ownerEmail, String operation) {
        Counter.builder(METRIC_BLOCKED_CASH_OPERATION)
                .tag("account_id", String.valueOf(accountId))
                .tag("owner_email", ownerEmail)
                .tag("operation", operation)
                .tag("service", "cash-service")
                .tag("status", "failure")
                .description("Allowed cash operation")
                .register(registry)
                .increment();

        log.debug("Заблокированная операция с наличными для пользователя с email {}", ownerEmail);
    }
}
