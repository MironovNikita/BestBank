package com.bank.controller;

import com.bank.dto.transfer.TransferOperationDto;
import com.bank.service.TransfersService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransfersController {

    private final TransfersService transfersService;

    @PostMapping
    public Mono<Void> operateTransfer(@Validated @RequestBody TransferOperationDto transfersOperation) {
        return transfersService.operateTransfer(transfersOperation);
    }
}
