package com.bank.common.config;

import com.bank.dto.email.EmailNotificationDto;
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

    public ProducerFactory<String, EmailNotificationDto> producerFactory() {
        Map<String, Object> configs = new HashMap<>();

        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        //Упрощаем сообщение -> чистый JSON, без возможности подмены типа
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        //Стратегия "At least once". all ожидает, что сообщение будет записано на все in-sync реплики
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        //Допускается не поддерживать очерёдность отправки сообщений, ставим одновременных сообщений 5
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        //Количество повторных попыток
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        //Убираем дубликаты при retry
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        //Таймаут ответа брокера на запрос
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);
        //Общий таймаут для запросов с учётом повторных попыток
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 15_000);

        //Backoff между retries
        configs.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        //Максимальный backoff
        configs.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 5_000);

        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, EmailNotificationDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
