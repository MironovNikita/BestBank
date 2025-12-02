package com.bank.service;

import com.bank.dto.currency.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRateGeneratorServiceImpl {
    //TODO Добавить интерфейс

    private static final Currency BASE_CURRENCY = Currency.RUB;
    private static final BigDecimal MIN_RATE = new BigDecimal("0.01");
    private static final BigDecimal MAX_RATE = new BigDecimal("3.0");

    private final ExchangeServiceClientImpl exchangeServiceClient;

    @Scheduled(fixedRate = 30000)
    public void generateCurrencyRate() {

        var rnd = ThreadLocalRandom.current();
        Map<Currency, BigDecimal> rates = new HashMap<>();
        rates.put(Currency.RUB, BigDecimal.valueOf(1.00));

        Arrays.stream(Currency.values())
                .filter(currency -> !BASE_CURRENCY.equals(currency))
                .forEach(currency -> {

                    double randomRate = MIN_RATE.doubleValue() + (MAX_RATE.doubleValue() - MIN_RATE.doubleValue()) * rnd.nextDouble();
                    BigDecimal rate = BigDecimal.valueOf(randomRate).setScale(2, RoundingMode.HALF_EVEN);

                    rates.put(currency, rate);
                });

        log.info("Сформированы новые курсы валют: {}", rates);

        exchangeServiceClient.updateExchange(rates).subscribe();
    }
}
