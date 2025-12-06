package com.bank.service;

import reactor.core.publisher.Mono;

public interface BlockerService {

    Mono<Boolean> checkOperation();
}
