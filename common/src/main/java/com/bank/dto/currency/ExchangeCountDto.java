package com.bank.dto.currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ExchangeCountDto {

    @Digits(integer = 10, fraction = 2, message = "Некорректный формат суммы")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @NotNull(message = "Сумма перевода не может быть пустой")
    private BigDecimal amount;

    @NotNull(message = "Валюта перевода не может быть пустой")
    private Currency originalCurrency;

    @NotNull(message = "Валюта перевода не может быть пустой")
    private Currency targetCurrency;
}
