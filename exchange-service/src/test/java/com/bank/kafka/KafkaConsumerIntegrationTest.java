package com.bank.kafka;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.UpdateRateDto;
import com.bank.service.ExchangeService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.kafka.consumer.group-id=exchange-rates-${random.uuid}")
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = "actual-rates", brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class KafkaConsumerIntegrationTest {

    private static final String TOPIC = "actual-rates";
    private static final String KEY = "exchange-rates";

    @MockitoSpyBean
    private ExchangeService exchangeService;

    private KafkaTemplate<String, UpdateRateDto> kafkaTemplate;

    @BeforeEach
    void initProducer() {
        when(exchangeService.updateCurrencyRates(any())).thenReturn(Mono.empty());

        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configs));
    }

    @Test
    @DisplayName("Проверка получения курсов валют и обработка для обновления")
    void shouldUpdateCurrencyRatesSuccessfully() {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, new BigDecimal("0.25"),
                Currency.EUR, new BigDecimal("0.10")
        );
        UpdateRateDto dto = new UpdateRateDto(rates, System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, KEY, dto);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(exchangeService, atLeastOnce())
                        .updateCurrencyRates(any()));
    }

    @Test
    @DisplayName("Проверка считывания только последнего сообщения")
    void shouldUpdateCurrencyRatesByLastMessage() {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, new BigDecimal("0.25"),
                Currency.EUR, new BigDecimal("0.10")
        );
        UpdateRateDto dto = new UpdateRateDto(rates, System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, KEY, dto);
        kafkaTemplate.send(TOPIC, KEY, dto);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(exchangeService, atLeast(1))
                        .updateCurrencyRates(any()));
    }
}
