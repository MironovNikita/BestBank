package com.bank.common.mapper;

import com.bank.dto.cash.CashOperationDto;
import com.bank.entity.CashOperation;
import org.springframework.stereotype.Component;

@Component
public class CashOperationMapper {
    public CashOperation toCashOperation(CashOperationDto dto) {
        CashOperation cashOperation = new CashOperation();
        cashOperation.setAccountId(dto.getAccountId());
        cashOperation.setAmount(dto.getAmount());
        cashOperation.setOperation(dto.getOperation());
        return cashOperation;
    }
}
