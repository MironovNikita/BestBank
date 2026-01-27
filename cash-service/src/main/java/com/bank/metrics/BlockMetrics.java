package com.bank.metrics;

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
        registry.counter(METRIC_ALLOWED_CASH_OPERATION,
                        "account_id", String.valueOf(accountId),
                        "owner_email", ownerEmail,
                        "operation", operation,
                        "service", "cash-service",
                        "status", "success")
                .increment();

        log.debug("Успешная операция с наличными для пользователя с email {}", ownerEmail);
    }

    public void recordBlockedOperation(Long accountId, String ownerEmail, String operation) {
        registry.counter(METRIC_BLOCKED_CASH_OPERATION,
                        "account_id", String.valueOf(accountId),
                        "owner_email", ownerEmail,
                        "operation", operation,
                        "service", "cash-service",
                        "status", "failure")
                .increment();

        log.debug("Заблокированная операция с наличными для пользователя с email {}", ownerEmail);
    }
}
