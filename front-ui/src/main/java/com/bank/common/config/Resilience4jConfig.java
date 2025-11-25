package com.bank.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public Retry accountsServiceRetry() {
        return Retry.ofDefaults("accountsServiceRetry");
    }

    @Bean
    public CircuitBreaker accountsServiceCB() {
        return CircuitBreaker.ofDefaults("accountsServiceCB");
    }

    @Bean
    public Retry cashServiceRetry() {
        return Retry.ofDefaults("cashServiceRetry");
    }

    @Bean
    public CircuitBreaker cashServiceCB() {
        return CircuitBreaker.ofDefaults("cashServiceCB");
    }

    @Bean
    public Retry transfersServiceRetry() {
        return Retry.ofDefaults("transfersServiceRetry");
    }

    @Bean
    public CircuitBreaker transfersServiceCB() {
        return CircuitBreaker.ofDefaults("transfersServiceCB");
    }
}
