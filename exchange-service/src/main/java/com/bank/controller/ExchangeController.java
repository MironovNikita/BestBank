package com.bank.controller;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.CurrencyRateDto;
import com.bank.dto.currency.ExchangeCountDto;
import com.bank.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @Deprecated
    @PostMapping("/update")
    public Mono<Void> updateCurrencyRates(@RequestBody Map<Currency, BigDecimal> rates) {
        return exchangeService.updateCurrencyRates(rates);
    }

    @GetMapping("/rates")
    public Flux<CurrencyRateDto> getCurrencyRates() {
        return exchangeService.getActualRates();
    }

    @PostMapping("/recount")
    public Mono<BigDecimal> recountAmount(@Validated @RequestBody ExchangeCountDto dto) {
        return exchangeService.recountAmount(dto);
    }
}
