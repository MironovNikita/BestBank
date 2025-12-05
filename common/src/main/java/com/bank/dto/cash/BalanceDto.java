package com.bank.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceDto {
    private Long id;
    private BigDecimal balance;
}
