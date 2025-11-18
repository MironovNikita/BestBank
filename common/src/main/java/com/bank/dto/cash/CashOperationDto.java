package com.bank.dto.cash;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashOperationDto {

    @Positive(message = "ID аккаунта не может быть отрицательным или 0")
    @NotNull(message = "ID аккаунта не может быть пустым")
    private Long accountId;

    @NotBlank(message = "Тип операции обязательно должен быть указан")
    private String operation;

    @Positive(message = "Сумма не может быть отрицательной или 0")
    @NotNull(message = "Сумма не может быть пустой")
    private Long amount;
}
