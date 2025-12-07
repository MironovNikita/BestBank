package com.bank.service;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.CurrencyRateDto;
import com.bank.dto.currency.ExchangeCountDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeService {

    Mono<Void> updateCurrencyRates(Map<Currency, BigDecimal> newRates);

    Flux<CurrencyRateDto> getActualRates();

    Mono<BigDecimal> recountAmount(ExchangeCountDto dto);
}
