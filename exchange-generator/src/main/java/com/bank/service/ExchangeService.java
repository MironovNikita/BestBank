package com.bank.service;

import com.bank.dto.currency.Currency;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeService {

    Mono<Void> updateExchange(Map<Currency, BigDecimal> rates);
}
