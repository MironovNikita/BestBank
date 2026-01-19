package com.bank.metrics;

import com.bank.dto.currency.Currency;
import io.micrometer.core.instrument.Counter;
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
        Counter.builder(METRIC_ALLOWED_TRANSFER_OPERATION)
                .tag("account_id_from", String.valueOf(accountIdFrom))
                .tag("account_id_to", String.valueOf(accountIdTo))
                .tag("currency_from", currencyFrom.name())
                .tag("currency_to", currencyTo.name())
                .tag("email", email)
                .tag("service", "transfer-service")
                .tag("status", "success")
                .description("Allowed transfer operation")
                .register(registry)
                .increment();

        log.debug("Успешная операция перевода для пользователя с email {}", email);
    }

    public void recordBlockedOperation(Long accountIdFrom, Currency currencyFrom, Long accountIdTo, Currency currencyTo, String email) {
        Counter.builder(METRIC_BLOCKED_TRANSFER_OPERATION)
                .tag("account_id_from", String.valueOf(accountIdFrom))
                .tag("account_id_to", String.valueOf(accountIdTo))
                .tag("currency_from", currencyFrom.name())
                .tag("currency_to", currencyTo.name())
                .tag("email", email)
                .tag("service", "transfer-service")
                .tag("status", "failure")
                .description("Blocked transfer operation")
                .register(registry)
                .increment();

        log.debug("Заблокированная операция перевода для пользователя с email {}", email);
    }
}
