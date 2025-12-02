package com.bank.controller;

import com.bank.dto.currency.Currency;
import com.bank.service.ExchangeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeServiceImpl exchangeService;

    @PostMapping("/update")
    public Mono<Void> updateCurrencyRates(@RequestBody Map<Currency, BigDecimal> rates) {
        return exchangeService.updateCurrencyRates(rates);
    }

    //TODO Добавить расчёт в текущей сумме по курсам в другой валюте
}
