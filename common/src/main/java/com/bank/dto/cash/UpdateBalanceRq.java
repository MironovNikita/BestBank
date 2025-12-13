package com.bank.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UpdateBalanceRq {
    private BigDecimal balance;
}
