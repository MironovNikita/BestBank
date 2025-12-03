package com.bank.dto.account;

import com.bank.dto.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountListDto {

    private Long id;
    private Long ownerId;
    private String title;
    private Currency currency;
    private BigDecimal balance;
}
