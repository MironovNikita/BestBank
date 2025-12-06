package com.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

@Slf4j
@Service
public class BlockerServiceImpl implements BlockerService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public Mono<Boolean> checkOperation() {
        Boolean check = secureRandom.nextDouble() < 0.8;
        log.info("Проверка текущей операции: {}", check);
        return Mono.just(check);
    }
}
