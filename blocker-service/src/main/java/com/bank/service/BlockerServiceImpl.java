package com.bank.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

@Service
public class BlockerServiceImpl {
    //TODO Добавить интерфейс

    private final SecureRandom secureRandom = new SecureRandom();

    public Mono<Boolean> checkOperation() {
        return Mono.just(secureRandom.nextDouble() < 0.8);
    }
}
