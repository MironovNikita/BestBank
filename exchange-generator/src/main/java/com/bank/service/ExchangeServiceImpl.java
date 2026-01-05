package com.bank.service;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.UpdateRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final KafkaTemplate<String, UpdateRateDto> kafkaTemplate;
    private final Clock clock;

    @Value("${spring.kafka.topic.exchange}")
    private String exchangeTopic;

    @Value("${spring.kafka.topic.exchange.key}")
    private String messagesKey;

    @Override
    public Mono<Void> updateExchange(Map<Currency, BigDecimal> rates) {

        UpdateRateDto dto = new UpdateRateDto(rates, clock.millis());

        return Mono.fromFuture(() -> kafkaTemplate.send(exchangeTopic, messagesKey, dto).toCompletableFuture())
                .doOnSuccess(result -> log.info("Курсы валют были успешно обновлены: {}, topic: {}, offset: {}",
                        rates,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset()))
                .doOnError(e -> log.error("Ошибка обновления курсов валют: {}", e.getMessage()))
                .then();
    }
}
