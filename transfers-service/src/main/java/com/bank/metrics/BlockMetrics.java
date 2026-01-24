package com.bank.metrics;

import com.bank.dto.currency.Currency;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_ALLOWED_TRANSFER_OPERATION;
import static com.bank.metrics.MetricConstants.METRIC_BLOCKED_TRANSFER_OPERATION;

@Slf4j
@Component
public class BlockMetrics {

    private final MeterRegistry registry;

    public BlockMetrics(MeterRegistry meterRegistry) {
        this.registry = meterRegistry;
    }

    public void recordAllowedOperation(Long accountIdFrom, Currency currencyFrom, Long accountIdTo, Currency currencyTo, String email) {
        registry.counter(METRIC_ALLOWED_TRANSFER_OPERATION,
                        "account_id_from", String.valueOf(accountIdFrom),
                        "account_id_to", String.valueOf(accountIdTo),
                        "currency_from", currencyFrom.name(),
                        "currency_to", currencyTo.name(),
                        "email", email,
                        "service", "transfer-service",
                        "status", "success")
                .increment();

        log.debug("Успешная операция перевода для пользователя с email {}", email);
    }

    public void recordBlockedOperation(Long accountIdFrom, Currency currencyFrom, Long accountIdTo, Currency currencyTo, String email) {
        registry.counter(METRIC_BLOCKED_TRANSFER_OPERATION,
                        "account_id_from", String.valueOf(accountIdFrom),
                        "account_id_to", String.valueOf(accountIdTo),
                        "currency_from", currencyFrom.name(),
                        "currency_to", currencyTo.name(),
                        "email", email,
                        "service", "transfer-service",
                        "status", "failure")
                .increment();

        log.debug("Заблокированная операция перевода для пользователя с email {}", email);
    }
}
