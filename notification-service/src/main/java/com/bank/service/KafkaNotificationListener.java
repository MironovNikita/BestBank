package com.bank.service;

import com.bank.dto.email.EmailNotificationDto;
import com.bank.metrics.NotificationMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaNotificationListener {

    private final EmailService emailService;
    private final NotificationMetrics notificationMetrics;

    @KafkaListener(topics = {
            "${spring.kafka.topic.accounts}",
            "${spring.kafka.topic.cash}",
            "${spring.kafka.topic.transfers}"
    },
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(EmailNotificationDto dto,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       Acknowledgment ack) {

        try {
            String email = dto.getTo();
            log.info("Получен запрос из Kafka (topic: {}) на отправку уведомления по email: {}", topic, email);
            emailService.sendEmail(email, dto.getSubject(), dto.getText());
            ack.acknowledge();
            notificationMetrics.recordSuccessfulNotification(email);
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления по запросу из Kafka на email: {}, {}", dto.getTo(), e.getMessage(), e);
            notificationMetrics.recordFailedNotification(dto.getTo());
        }
    }
}
