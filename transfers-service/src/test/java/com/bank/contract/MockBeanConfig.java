package com.bank.contract;

import com.bank.service.TransfersService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Profile("test")
@TestConfiguration
public class MockBeanConfig {

    @Bean
    public TransfersService transfersService() {
        TransfersService mock = Mockito.mock(TransfersService.class);

        when(mock.operateTransfer(any())).thenReturn(Mono.empty());

        return mock;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
