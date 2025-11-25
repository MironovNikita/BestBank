package com.bank.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceDto {
    private Long id;
    private Long balance;
}
