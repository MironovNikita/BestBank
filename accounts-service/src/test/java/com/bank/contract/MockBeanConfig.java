package com.bank.contract;

import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.login.LoginResponse;
import com.bank.service.AccountService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Configuration
public class MockBeanConfig {

    @Bean
    public AccountService accountService() {
        AccountService mock = Mockito.mock(AccountService.class);

        when(mock.getAllAccounts(anyLong()))
                .thenReturn(
                        Flux.just(
                                new AccountMainPageDto("1", "89996665522", "Test", "Test"),
                                new AccountMainPageDto("2", "89106665522", "Ne test", "Ne test")
                        )
                );

        when(mock.getBalance(anyLong()))
                .thenReturn(Mono.just(new BalanceDto(3L, 1000L)));

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(3L).setEmail("test@test.ru").setName("Test");
        when(mock.login(any())).thenReturn(Mono.just(loginResponse));

        when(mock.editAccount(anyLong(), any())).thenReturn(Mono.empty());

        when(mock.editPassword(anyLong(), any())).thenReturn(Mono.empty());

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
