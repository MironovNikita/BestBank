package com.bank.service;

import reactor.core.publisher.Mono;

public interface NotificationsService {
    Mono<Void> sendNotification(String toEmail, String subject, String text);
}
