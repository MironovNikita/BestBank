package com.bank.service;

import reactor.core.publisher.Mono;

public interface NotificationsService {

    Mono<Void> sendTransferNotification(String email, String subject, String text);
}
