package com.bank.common.mapper;

import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import org.springframework.stereotype.Component;

@Component
public class TransferOperationMapper {
    public TransferOperation toTransferOperation(TransferOperationDto transferOperationDto) {
        TransferOperation transferOperation = new TransferOperation();
        transferOperation.setAccountIdFrom(transferOperationDto.getAccountIdFrom());
        transferOperation.setAccountIdTo(transferOperationDto.getAccountIdTo());
        transferOperation.setCurrencyFrom(transferOperationDto.getCurrencyFrom());
        transferOperation.setAmountFrom(transferOperationDto.getAmountFrom());
        transferOperation.setCurrencyTo(transferOperationDto.getCurrencyTo());
        transferOperation.setAmountTo(transferOperationDto.getAmountTo() == null ? transferOperationDto.getAmountFrom() : transferOperationDto.getAmountTo());
        return transferOperation;
    }
}
