package com.bank.service;

import com.bank.dto.transfer.TransferOperationDto;
import reactor.core.publisher.Mono;

public interface TransfersService {

    Mono<Void> operateTransfer(TransferOperationDto transferOperationDto);
}
