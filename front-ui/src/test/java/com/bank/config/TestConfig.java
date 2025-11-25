package com.bank.config;

import com.bank.security.SecureBase64Converter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    public WebClient webClientBuilder() {
        return WebClient.builder().build();
    }

    @Bean
    public Retry retry() {
        return Mockito.mock(Retry.class);
    }

    @Bean
    public CircuitBreaker circuitBreaker() {
        return Mockito.mock(CircuitBreaker.class);
    }

    @Bean
    SecureBase64Converter secureBase64Converter() {
        return Mockito.mock(SecureBase64Converter.class);
    }

    @Bean
    ServerSecurityContextRepository serverSecurityContextRepository() {
        return Mockito.mock(ServerSecurityContextRepository.class);
    }

    @Bean
    ReactiveAuthenticationManager authenticationManager() {
        return Mockito.mock(ReactiveAuthenticationManager.class);
    }
}
