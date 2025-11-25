package com.bank.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public Retry notificationsServiceRetry() {
        return Retry.ofDefaults("notificationsServiceRetry");
    }

    @Bean
    public CircuitBreaker notificationsServiceCB() {
        return CircuitBreaker.ofDefaults("notificationsServiceCB");
    }
}
