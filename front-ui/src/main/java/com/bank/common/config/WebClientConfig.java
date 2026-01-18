package com.bank.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient accountsWebClient(WebClient.Builder builder,
                                       ReactiveClientRegistrationRepository clients,
                                       ServerOAuth2AuthorizedClientRepository authClients,
                                       @Value("${services.accounts.base-url}") String accountsBaseUrl) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("accounts-service");
        return builder
                .clone()
                .baseUrl(accountsBaseUrl)
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient cashWebClient(WebClient.Builder builder,
                                   ReactiveClientRegistrationRepository clients,
                                   ServerOAuth2AuthorizedClientRepository authClients,
                                   @Value("${services.cash.base-url}") String cashBaseUrl) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("cash-service");
        return builder
                .clone()
                .baseUrl(cashBaseUrl)
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient transfersWebClient(WebClient.Builder builder,
                                        ReactiveClientRegistrationRepository clients,
                                        ServerOAuth2AuthorizedClientRepository authClients,
                                        @Value("${services.transfers.base-url}") String transfersBaseUrl) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("transfers-service");
        return builder
                .clone()
                .baseUrl(transfersBaseUrl)
                .filter(oauth)
                .build();
    }

    @Bean
    public WebClient exchangeServiceWebClient(WebClient.Builder builder,
                                              ReactiveClientRegistrationRepository clients,
                                              ServerOAuth2AuthorizedClientRepository authClients,
                                              @Value("${services.exchange.base-url}") String exchangeBaseUrl) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);

        oauth.setDefaultClientRegistrationId("exchange-service");
        return builder
                .clone()
                .baseUrl(exchangeBaseUrl)
                .filter(oauth)
                .build();
    }
}
