package com.bank.contract.account;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("test")
@TestConfiguration
public class TestConfig {

    @Bean
    public WebClient accountsWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8100")
                .build();
    }

    @Bean
    public Retry accountsServiceRetry() {
        return Retry.ofDefaults("accountsServiceRetry");
    }

    @Bean
    public CircuitBreaker accountsServiceCB() {
        return CircuitBreaker.ofDefaults("accountsServiceCB");
    }
}
