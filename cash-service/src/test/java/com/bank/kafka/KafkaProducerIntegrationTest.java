package com.bank.kafka;

import com.bank.common.config.KafkaProducerConfig;
import com.bank.common.config.Resilience4jConfig;
import com.bank.dto.email.EmailNotificationDto;
import com.bank.service.NotificationsService;
import com.bank.service.NotificationsServiceImpl;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EmbeddedKafka(partitions = 3, topics = "cash-notifications", brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@SpringBootTest(classes = {NotificationsServiceImpl.class, KafkaProducerConfig.class, Resilience4jConfig.class})
@ActiveProfiles("test")
public class KafkaProducerIntegrationTest {

    @Autowired
    private NotificationsService notificationsService;

    private static final String TOPIC = "cash-notifications";
    private KafkaMessageListenerContainer<String, EmailNotificationDto> container;
    private BlockingQueue<ConsumerRecord<String, EmailNotificationDto>> records;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.topic.notification", () -> TOPIC);
    }

    @BeforeEach
    void initConsumer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test" + UUID.randomUUID());
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EmailNotificationDto.class.getName());

        DefaultKafkaConsumerFactory<String, EmailNotificationDto> consumerFactory = new DefaultKafkaConsumerFactory<>(configs);

        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, EmailNotificationDto>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, 3);

        records.clear();
    }

    @AfterEach
    void stopConsumer() {
        if (container != null) container.stop();
    }

    @Test
    @DisplayName("Проверка успешной отправки email-уведомления через Kafka")
    void shouldSendEmailNotificationSuccessfully() throws InterruptedException {
        String emailTo = "test@test.ru";
        String subject = "Test subject";
        String text = "Test text";

        notificationsService.sendNotification(emailTo, subject, text).block();

        ConsumerRecord<String, EmailNotificationDto> record = records.poll(5, TimeUnit.SECONDS);
        assertNotNull(record);
        EmailNotificationDto dto = record.value();
        assertNotNull(dto);
        assertEquals(dto.getTo(), emailTo);
        assertEquals(dto.getSubject(), subject);
        assertEquals(dto.getText(), text);

        assertNull(records.poll(3, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Проверка отправки нескольких email-уведомлений через Kafka")
    void shouldSendSeveralEmailNotificationsSuccessfully() throws InterruptedException {
        String emailTo = "test@test.ru";
        String subject = "Test subject";
        String text = "Test text";
        int quantity = 3;

        for (int i = 0; i < quantity; i++) {
            notificationsService.sendNotification(emailTo, subject, text).block();
        }

        List<ConsumerRecord<String, EmailNotificationDto>> checking = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            ConsumerRecord<String, EmailNotificationDto> record = records.poll(10, TimeUnit.SECONDS);
            assertNotNull(record);
            assertNotNull(record.value());
            checking.add(record);
        }

        assertEquals(quantity, checking.size());
        assertNull(records.poll(3, TimeUnit.SECONDS));
    }
}
