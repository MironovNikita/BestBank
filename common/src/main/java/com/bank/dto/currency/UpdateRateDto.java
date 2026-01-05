package com.bank.dto.currency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRateDto {

    private Map<Currency, BigDecimal> rates;

    private Long generationTime;
}
