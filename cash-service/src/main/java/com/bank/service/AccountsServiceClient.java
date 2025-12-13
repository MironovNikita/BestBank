package com.bank.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountsServiceClient {

    Mono<BigDecimal> getCurrentAccountBalance(Long accountId);

    Mono<Void> updateRemoteBalance(BigDecimal newBalance, Long accountId);
}
