package com.bank.dto.cash;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CashOperationDto {

    @Positive(message = "ID счёта не может быть отрицательным или 0")
    @NotNull(message = "ID счёта не может быть пустым")
    private Long id;

    @Positive(message = "ID собственника счёта не может быть отрицательным или 0")
    @NotNull(message = "ID собственника счёта не может быть пустым")
    private Long ownerId;

    @NotBlank(message = "Тип операции обязательно должен быть указан")
    private String operation;

    @NotBlank(message = "Email обязательно должен быть указан")
    private String email;

    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @NotNull(message = "Сумма не может быть пустой")
    private BigDecimal amount;
}
