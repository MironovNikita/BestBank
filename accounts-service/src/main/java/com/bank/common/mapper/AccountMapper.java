package com.bank.common.mapper;

import com.bank.dto.account.AccountCreateDto;
import com.bank.entity.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountMapper {

    public Account toAccount(AccountCreateDto dto, Long userId) {
        return new Account(
                null,
                userId,
                dto.getTitle(),
                dto.getCurrency(),
                BigDecimal.valueOf(0)
        );
    }
}
