package com.bank.contract;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.CurrencyRateDto;
import com.bank.dto.currency.ExchangeCountDto;
import com.bank.service.ExchangeService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Profile("test")
@Configuration
public class MockBeanConfig {

    @Bean
    public ExchangeService exchangeService() {
        ExchangeService mock = Mockito.mock(ExchangeService.class);

        when(mock.updateCurrencyRates(any())).thenReturn(Mono.empty());

        CurrencyRateDto currencyRateDto1 = new CurrencyRateDto(Currency.RUB, BigDecimal.ONE, BigDecimal.ONE);
        CurrencyRateDto currencyRateDto2 = new CurrencyRateDto(Currency.USD, BigDecimal.valueOf(5.43), BigDecimal.valueOf(6.12));
        CurrencyRateDto currencyRateDto3 = new CurrencyRateDto(Currency.EUR, BigDecimal.valueOf(2.15), BigDecimal.valueOf(3.25));
        when(mock.getActualRates()).thenReturn(Flux.just(currencyRateDto1, currencyRateDto2, currencyRateDto3));

        ExchangeCountDto countDto = new ExchangeCountDto(BigDecimal.valueOf(200), Currency.RUB, Currency.EUR);
        when(mock.recountAmount(countDto)).thenReturn(Mono.just(BigDecimal.valueOf(2)));

        return mock;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(@Autowired(required = false) ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
