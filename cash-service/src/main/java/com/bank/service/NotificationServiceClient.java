package com.bank.service;

import reactor.core.publisher.Mono;

public interface NotificationServiceClient {

    Mono<Void> sendNotification(String toEmail, String subject, String text);
}
