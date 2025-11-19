package com.bank.service;

import com.bank.dto.cash.CashOperationDto;
import reactor.core.publisher.Mono;

public interface CashService {

    Mono<Void> operateCash(CashOperationDto cashOperationDto);
}
