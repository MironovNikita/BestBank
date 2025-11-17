package com.bank.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${spring.cloud.gateway.discovery.routes[0].uri}")
    private String accountServiceUrl;

    @Bean
    @LoadBalanced
    public WebClient.Builder accountsWebClientBuilder() {
        return WebClient.builder()
                .baseUrl(accountServiceUrl);
    }

    @Bean
    public WebClient accountsWebClient() {
        return accountsWebClientBuilder().build();
    }
}
