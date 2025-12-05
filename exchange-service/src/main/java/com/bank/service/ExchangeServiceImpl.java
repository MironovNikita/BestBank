package com.bank.service;

import com.bank.common.exception.CurrencyException;
import com.bank.dto.currency.Currency;
import com.bank.dto.currency.CurrencyRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl {
    //TODO Добавить интерфейс

    private static final Map<Currency, BigDecimal> RATES = new HashMap<>();
    private static final BigDecimal SPREAD = new BigDecimal("0.3");
    private static final MathContext MC = new MathContext(2, RoundingMode.HALF_EVEN);

    public Mono<Void> updateCurrencyRates(Map<Currency, BigDecimal> newRates) {
        return Mono.fromRunnable(() -> {
            synchronized (RATES) {
                RATES.clear();
                RATES.putAll(newRates);
                log.info("Курсы валют обновлены успешно: {}", RATES);
            }
        });
    }

    //@Override
    public Flux<CurrencyRateDto> getActualRates() {

        if (RATES.isEmpty()) return Flux.error(new CurrencyException("Ошибка получения актуальных курсов валют. Расчёты временно приостановлены"));

        return Flux.fromIterable(RATES.entrySet())
                .doOnSubscribe(s -> log.info("Производится расчёт актуальных курсов покупки/продажи валют"))
                .filter(rate -> !(rate.getKey() == Currency.RUB))
                .map(entry -> {
                    Currency currency = entry.getKey();
                    BigDecimal rate = RATES.get(currency);

                    return new CurrencyRateDto(
                            currency,
                            rate.multiply(BigDecimal.ONE.subtract(SPREAD), MC),
                            rate.multiply(BigDecimal.ONE.add(SPREAD), MC)
                    );
                });
    }

    //TODO TransferOperationDto??? Скорее нужно какое-то новое DTO: сумму, исходную валюту, искомую валюту
    public BigDecimal getRate() {
        return null;
    }
}
