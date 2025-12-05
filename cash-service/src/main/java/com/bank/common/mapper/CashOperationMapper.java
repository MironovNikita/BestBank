package com.bank.common.mapper;

import com.bank.dto.cash.CashOperationDto;
import com.bank.entity.CashOperation;
import com.bank.entity.OperationType;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class CashOperationMapper {
    public CashOperation toCashOperation(CashOperationDto dto) {
        CashOperation cashOperation = new CashOperation();
        cashOperation.setAccountId(dto.getId());
        cashOperation.setUserId(dto.getOwnerId());
        cashOperation.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        cashOperation.setOperation(OperationType.valueOf(dto.getOperation()));
        return cashOperation;
    }
}
