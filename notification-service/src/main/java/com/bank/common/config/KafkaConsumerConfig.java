package com.bank.common.config;

import com.bank.dto.email.EmailNotificationDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServersConfig;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, EmailNotificationDto> consumerFactory() {

        Map<String, Object> configs = new HashMap<>();

        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EmailNotificationDto.class.getName());

        //Количество одновременно обрабатываемых сообщений
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        //Ручное управление коммитами
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        //Режим чтения
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        //Таймаут сессии consumer
        configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30_000);
        //Интервал проверки доступности consumer
        configs.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10_000);
        //Максимальный интервал между вызовами consumer.poll()
        configs.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailNotificationDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmailNotificationDto> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);

        return factory;
    }
}
