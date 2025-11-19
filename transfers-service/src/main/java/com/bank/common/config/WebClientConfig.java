package com.bank.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient accountsWebClient() {
        return webClientBuilder().baseUrl("lb://accounts-service").build();
    }

    @Bean
    public WebClient notificationsWebClient() {
        return webClientBuilder().baseUrl("lb://notification-service").build();
    }
}
