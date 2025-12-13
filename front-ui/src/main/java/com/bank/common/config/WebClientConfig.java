package com.bank.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient accountsWebClient(ReactiveClientRegistrationRepository clients,
                                       ServerOAuth2AuthorizedClientRepository authClients) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("accounts-service");
        return loadBalancedWebClientBuilder()
                .clone()
                .baseUrl("http://accounts-service:8081")
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient cashWebClient(ReactiveClientRegistrationRepository clients,
                                   ServerOAuth2AuthorizedClientRepository authClients) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("cash-service");
        return loadBalancedWebClientBuilder()
                .clone()
                .baseUrl("http://cash-service:8083")
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient transfersWebClient(ReactiveClientRegistrationRepository clients,
                                        ServerOAuth2AuthorizedClientRepository authClients) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("transfers-service");
        return loadBalancedWebClientBuilder()
                .clone()
                .baseUrl("http://transfers-service:8082")
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient exchangeServiceWebClient(
            ReactiveClientRegistrationRepository clients,
            ServerOAuth2AuthorizedClientRepository authClients
    ) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("exchange-service");
        return loadBalancedWebClientBuilder()
                .clone()
                .baseUrl("http://exchange-service:8087")
                .filter(oauth)
                .build();
    }
}
