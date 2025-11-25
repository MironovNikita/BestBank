package com.bank.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateBalanceRq {
    private Long balance;
}
