package com.bank.contract;

import com.bank.service.EmailService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@Configuration
public class MockBeanConfig {

    @Bean
    public EmailService emailService() {
        EmailService mock = Mockito.mock(EmailService.class);

        doNothing().when(mock).sendEmail(anyString(), anyString(), anyString());

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
