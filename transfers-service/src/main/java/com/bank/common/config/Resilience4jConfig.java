package com.bank.common.config;

import com.bank.common.exception.TransferException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final RetryRegistry retryRegistry;

    @Bean
    public Retry accountsServiceRetry() {
        Retry retry = retryRegistry.retry("accountsServiceRetry");

        RetryConfig newConfig = RetryConfig.from(retry.getRetryConfig())
                .ignoreExceptions(TransferException.class)
                .build();

        return Retry.of("accountsServiceRetry", newConfig);
    }

    @Bean
    public CircuitBreaker accountsServiceCB() {
        return CircuitBreaker.ofDefaults("accountsServiceCB");
    }

    @Bean
    public Retry notificationsServiceRetry() {
        return Retry.ofDefaults("notificationsServiceRetry");
    }

    @Bean
    public CircuitBreaker notificationsServiceCB() {
        return CircuitBreaker.ofDefaults("notificationsServiceCB");
    }
}
