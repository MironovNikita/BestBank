package com.bank.service;

import com.bank.dto.currency.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl {
    //TODO Добавить интерфейс

    private final Map<Currency, BigDecimal> rates = new HashMap<>();

    public Mono<Void> updateCurrencyRates(Map<Currency, BigDecimal> newRates) {
        return Mono.fromRunnable(() -> {
            synchronized (rates) {
                rates.clear();
                rates.putAll(newRates);
                log.info("Курсы валют обновлены успешно: {}", rates);
            }
        });
    }

    //TODO TransferOperationDto??? Скорее нужно какое-то новое DTO: сумму, исходную валюту, искомую валюту
    public BigDecimal getRate() {
        return null;
    }
}
