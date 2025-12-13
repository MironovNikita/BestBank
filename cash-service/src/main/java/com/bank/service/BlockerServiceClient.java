package com.bank.service;

import reactor.core.publisher.Mono;

public interface BlockerServiceClient {

    Mono<Boolean> checkOperation();
}
