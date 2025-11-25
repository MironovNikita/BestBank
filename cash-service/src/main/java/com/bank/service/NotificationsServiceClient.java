package com.bank.service;

import reactor.core.publisher.Mono;

public interface NotificationsServiceClient {

    Mono<Void> sendNotification(String toEmail, String subject, String text);
}
