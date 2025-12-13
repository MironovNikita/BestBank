package com.bank.contract;

import com.bank.service.BlockerService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@Configuration
public class MockBeanConfig {

    @Bean
    public BlockerService blockerService() {
        BlockerService mock = Mockito.mock(BlockerService.class);

        when(mock.checkOperation()).thenReturn(Mono.just(true));

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
