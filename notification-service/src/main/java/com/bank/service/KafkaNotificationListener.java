package com.bank.service;

import com.bank.dto.email.EmailNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaNotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = {
            "${spring.kafka.topic.accounts}",
            "${spring.kafka.topic.cash}",
            "${spring.kafka.topic.transfers}"
    },
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(EmailNotificationDto dto, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            log.info("Получен запрос из Kafka (topic: {}) на отправку уведомления по email: {}", topic, dto.getTo());
            emailService.sendEmail(dto.getTo(), dto.getSubject(), dto.getText());
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления по запросу из Kafka на email: {}, {}", dto.getTo(), e.getMessage());
        }
    }
}
