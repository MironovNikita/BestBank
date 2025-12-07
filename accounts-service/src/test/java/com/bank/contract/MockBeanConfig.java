package com.bank.contract;

import com.bank.dto.account.AccountEditDto;
import com.bank.dto.account.AccountListDto;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.currency.Currency;
import com.bank.dto.login.LoginResponse;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Configuration
public class MockBeanConfig {

    @Bean
    public AccountService accountService() {
        AccountService mock = Mockito.mock(AccountService.class);

        when(mock.getUserAccounts(anyLong()))
                .thenReturn(
                        Flux.just(
                                new AccountListDto(1L, 3L, "Test", Currency.RUB, BigDecimal.valueOf(1000)),
                                new AccountListDto(2L, 3L, "Test", Currency.EUR, BigDecimal.valueOf(10))
                        )
                );

        when(mock.getAccountBalance(anyLong()))
                .thenReturn(Mono.just(new BalanceDto(3L, BigDecimal.valueOf(1000))));

        AccountEditDto accountEditDto = new AccountEditDto();
        accountEditDto.setId(2L);
        accountEditDto.setNewTitle("New Title");
        accountEditDto.setCurrency(Currency.RUB);
        accountEditDto.setEmail("test@test.ru");
        when(mock.editAccount(accountEditDto)).thenReturn(Mono.empty());

        when(mock.updateBalance(anyLong(), any())).thenReturn(Mono.empty());

        when(mock.transfer(any())).thenReturn(Mono.empty());

        return mock;
    }

    //TODO Дополнить методы

    @Bean
    public UserService userService() {
        UserService mock = Mockito.mock(UserService.class);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(3L).setEmail("test@test.ru").setName("Test");
        when(mock.login(any())).thenReturn(Mono.just(loginResponse));

        UserPasswordChangeDto uPDto = new UserPasswordChangeDto("Password1111", "Password1111");
        when(mock.editPassword(3L, uPDto)).thenReturn(Mono.empty());

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
