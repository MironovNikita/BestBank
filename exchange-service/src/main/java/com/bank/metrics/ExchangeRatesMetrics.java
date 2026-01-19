package com.bank.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bank.metrics.MetricConstants.METRIC_FAILED_RATE_UPDATE;
import static com.bank.metrics.MetricConstants.METRIC_SUCCESS_RATE_UPDATE;

@Slf4j
@Component
public class ExchangeRatesMetrics {

    private final MeterRegistry registry;

    public ExchangeRatesMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccessfulRatesUpdate(int currenciesSize) {
        Counter.builder(METRIC_SUCCESS_RATE_UPDATE)
                .tag("status", "success")
                .tag("service", "exchange-service")
                .description("Successful currency rates update")
                .register(registry)
                .increment();

        log.debug("Успешное обновление курсов валют в количестве: {} шт.", currenciesSize);
    }

    public void recordFailedRatesUpdate(String message) {
        Counter.builder(METRIC_FAILED_RATE_UPDATE)
                .tag("status", "failure")
                .tag("service", "exchange-service")
                .description("Successful currency rates update")
                .register(registry)
                .increment();

        log.debug("Неудачное обновление курсов валют по причине: {}", message);
    }
}
