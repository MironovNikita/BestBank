package com.bank.config;

import com.bank.dto.currency.UpdateRateDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServersConfig;

    @Bean
    public ProducerFactory<String, UpdateRateDto> producerFactory() {
        Map<String, Object> configs = new HashMap<>();

        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        //Упрощаем сообщение -> чистый JSON, без возможности подмены типа
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        //Стратегия "At most once". 0 не ждёт подтверждения от брокера
        configs.put(ProducerConfig.ACKS_CONFIG, "0");
        //Необходимо поддерживать очерёдность отправки сообщений
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        //Количество повторных попыток - 0, согласно "At most once"
        configs.put(ProducerConfig.RETRIES_CONFIG, 0);
        //Стратегия "At most once" - выключаем идемпотентность
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);

        //Таймаут ответа брокера на запрос
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);
        //Общий таймаут для запросов с учётом повторных попыток
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10_000);

        //Отправляем батч сразу при его заполнении или вызове
        configs.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        //Максимальный размер одного батча для одной партиции
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, UpdateRateDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
