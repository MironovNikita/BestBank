package com.bank.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public Retry exchangeServiceRetry() {
        return Retry.ofDefaults("exchangeServiceRetry");
    }

    @Bean
    public CircuitBreaker exchangeServiceCB() {
        return CircuitBreaker.ofDefaults("exchangeServiceCB");
    }
}
