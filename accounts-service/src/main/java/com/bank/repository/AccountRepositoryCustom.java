package com.bank.repository;

import com.bank.dto.transfer.TransferOperationDto;
import reactor.core.publisher.Mono;

public interface AccountRepositoryCustom {

    Mono<Void> transfer(TransferOperationDto transferOperationDto);
}
