package com.bank.service;

import com.bank.dto.currency.UpdateRateDto;
import com.bank.metrics.ExchangeRatesMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaExchangeListener {

    private final ExchangeService exchangeService;
    private final ExchangeRatesMetrics ratesMetrics;

    @KafkaListener(topics = "${spring.kafka.topic.exchange}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(UpdateRateDto dto,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Получен запрос из Kafka (topic: {} на обновление курсов валют)", topic);

        exchangeService.updateCurrencyRates(dto.getRates())
                .doOnSuccess(s -> {
                    log.info("Курсы валют были успешно обновлены");
                    ratesMetrics.recordSuccessfulRatesUpdate(dto.getRates().size());
                })
                .doOnError(e -> {
                    String exceptionMessage = e.getMessage();
                    log.error("Ошибка обновления курсов валют: {}", exceptionMessage, e);
                    ratesMetrics.recordFailedRatesUpdate(exceptionMessage);
                })
                .subscribe();
    }
}
