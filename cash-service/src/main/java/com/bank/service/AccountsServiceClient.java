package com.bank.service;

import reactor.core.publisher.Mono;

public interface AccountsServiceClient {

    Mono<Long> getCurrentBalance(Long accountId);

    Mono<Void> updateRemoteBalance(Long newBalance, Long accountId);
}
