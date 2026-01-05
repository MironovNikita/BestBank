package com.bank.kafka;

import com.bank.dto.currency.Currency;
import com.bank.dto.currency.UpdateRateDto;
import com.bank.service.ExchangeService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"actual-rates"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9094", "port=9094"})
@ActiveProfiles("test")
public class KafkaProducerIntegrationTest {

    @Autowired
    private ExchangeService exchangeService;

    private static final String TOPIC = "actual-rates";
    private static final String KEY = "exchange-rates";
    private KafkaMessageListenerContainer<String, UpdateRateDto> container;
    private BlockingQueue<ConsumerRecord<String, UpdateRateDto>> records;

    @BeforeEach
    void initConsumer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test" + UUID.randomUUID());
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UpdateRateDto.class.getName());

        DefaultKafkaConsumerFactory<String, UpdateRateDto> consumerFactory = new DefaultKafkaConsumerFactory<>(configs);

        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, UpdateRateDto>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !records.isEmpty() || container.isRunning());
        records.clear();
    }

    @AfterEach
    void stopConsumer() {
        if (container != null) container.stop();
    }

    @Test
    @DisplayName("Проверка отправки сообщения с курсами валют")
    void shouldSendExchangeRatesSuccessfully() {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, new BigDecimal("0.25"),
                Currency.EUR, new BigDecimal("0.10")
        );

        exchangeService.updateExchange(rates).block();

        await().atMost(5, TimeUnit.SECONDS).until(() -> !records.isEmpty());
        ConsumerRecord<String, UpdateRateDto> record = records.poll();
        assertNotNull(record);
        assertEquals(KEY, record.key());
        UpdateRateDto dto = record.value();
        assertNotNull(dto);
        assertEquals(0, dto.getRates().get(Currency.RUB).compareTo(BigDecimal.ONE));
        assertEquals(0, dto.getRates().get(Currency.USD).compareTo(new BigDecimal("0.25")));
        assertEquals(0, dto.getRates().get(Currency.EUR).compareTo(new BigDecimal("0.10")));
    }

    @Test
    @DisplayName("Проверка режима отправки \"At most once\"")
    void shouldSendMessageWithCorrectKey() {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, new BigDecimal("0.25"),
                Currency.EUR, new BigDecimal("0.10")
        );

        long start = System.currentTimeMillis();
        exchangeService.updateExchange(rates).block();
        assertTrue(System.currentTimeMillis() - start < 100);
    }

    @Test
    @DisplayName("Проверка корректной очерёдности сообщений")
    void shouldMakeCorrectOrderOfMessages() {
        int quantity = 5;
        Map<Currency, BigDecimal> rates = new HashMap<>(Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, new BigDecimal("0.25"),
                Currency.EUR, new BigDecimal("0.10")
        ));

        for (int i = 1; i <= quantity; i++) {
            rates.put(Currency.USD, new BigDecimal("0.25" + i));

            exchangeService.updateExchange(rates).block();
        }

        BigDecimal previous = BigDecimal.ZERO;
        for (int i = 1; i <= quantity; i++) {
            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> !records.isEmpty());

            ConsumerRecord<String, UpdateRateDto> record = records.poll();
            assertNotNull(record);
            BigDecimal current = record.value().getRates().get(Currency.USD);
            assertNotNull(current);
            assertEquals(1, current.compareTo(previous));
            previous = current;
        }
    }
}
