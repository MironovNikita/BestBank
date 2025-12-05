package com.bank.dto.currency;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyRateDto {

    private Currency currency;
    private BigDecimal buy;
    private BigDecimal sell;
}
