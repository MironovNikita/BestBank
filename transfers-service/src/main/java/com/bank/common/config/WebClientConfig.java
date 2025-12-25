package com.bank.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder loadBalancedBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient accountsWebClient(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            WebClient.Builder builder
    ) {
        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultOAuth2AuthorizedClient(true);
        oauth.setDefaultClientRegistrationId("accounts-service");
        //TODO Временно для локальных тестов
        return builder
                //.baseUrl("http://accounts-service:8081")
                .baseUrl("http://localhost:8081")
                .filter(oauth)
                .build();
    }

    /*@Bean
    public WebClient notificationsWebClient(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            WebClient.Builder builder
    ) {
        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultOAuth2AuthorizedClient(true);
        oauth.setDefaultClientRegistrationId("notifications-service");

        return builder
                .baseUrl("http://notification-service:8084")
                .filter(oauth)
                .build();
    }*/

    @Bean
    public WebClient exchangeWebClient(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            WebClient.Builder builder
    ) {
        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultOAuth2AuthorizedClient(true);
        oauth.setDefaultClientRegistrationId("exchange-service");
        //TODO Временно для локальных тестов
        return builder
                //.baseUrl("http://exchange-service:8087")
                .baseUrl("http://localhost:8087")
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient blockerWebClient(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            WebClient.Builder builder
    ) {
        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultOAuth2AuthorizedClient(true);
        oauth.setDefaultClientRegistrationId("blocker-service");
        //TODO Временно для локальных тестов
        return builder
                //.baseUrl("http://blocker-service:8086")
                .baseUrl("http://localhost:8086")
                .filter(oauth)
                .build();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository registrations,
            ReactiveOAuth2AuthorizedClientService clientService
    ) {
        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                registrations, clientService
        );

        manager.setAuthorizedClientProvider(provider);

        return manager;
    }
}
