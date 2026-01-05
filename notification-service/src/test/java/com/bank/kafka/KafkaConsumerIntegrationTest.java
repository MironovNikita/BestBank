package com.bank.kafka;

import com.bank.dto.email.EmailNotificationDto;
import com.bank.service.EmailServiceImpl;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, topics = {"accounts-notifications"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092"})
public class KafkaConsumerIntegrationTest {

    private static final String TOPIC_ACC = "accounts-notifications";
    private static final String TOPIC_CASH = "cash-notifications";
    private static final String TOPIC_TRS = "transfers-notifications";

    @MockitoSpyBean
    private EmailServiceImpl emailService;
    @MockitoBean
    private JavaMailSender mailSender;

    private KafkaTemplate<String, EmailNotificationDto> kafkaTemplate;

    @BeforeEach
    void initProducer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configs));
    }

    @Test
    @DisplayName("Проверка обработки сообщения через Consumer")
    void shouldHandleMessageSuccessfully() {
        kafkaTemplate.send(TOPIC_ACC, new EmailNotificationDto("test@test.ru", "subject", "text"));

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(emailService, atLeastOnce())
                        .sendEmail(anyString(), anyString(), anyString()));
    }

    @Test
    @DisplayName("Проверка обработки нескольких сообщений Consumer")
    void shouldHandleSeveralMessagesWithDifferentTopics() {
        int quantity = 2;

        for (int i = 0; i < quantity; i++) {
            kafkaTemplate.send(TOPIC_CASH, new EmailNotificationDto("test@test.ru", "subject", "text"));
            kafkaTemplate.send(TOPIC_TRS, new EmailNotificationDto("test@test.ru", "subject", "text"));
        }

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(emailService, atLeast(4))
                        .sendEmail(anyString(), anyString(), anyString()));
    }
}
