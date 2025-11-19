package com.bank.controller;

import com.bank.dto.cash.CashOperationDto;
import com.bank.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping
    public Mono<Void> operateCash(@RequestBody @Validated CashOperationDto cashOperation) {
        return cashService.operateCash(cashOperation);
    }
}
