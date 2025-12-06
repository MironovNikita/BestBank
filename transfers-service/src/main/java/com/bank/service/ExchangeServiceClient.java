package com.bank.service;

import com.bank.dto.transfer.TransferOperationDto;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ExchangeServiceClient {

    Mono<BigDecimal> recountTransferAmount(TransferOperationDto dto);
}
