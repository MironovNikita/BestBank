package com.bank.dto.transfer;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferOperationDto {

    @Positive(message = "ID аккаунта отправителя не может быть отрицательным или 0")
    @NotNull(message = "ID аккаунта отправителя не может быть пустым")
    private Long accountIdFrom;

    @Positive(message = "ID аккаунта получателя отправителя не может быть отрицательным или 0")
    @NotNull(message = "ID аккаунта получателя отправителя не может быть пустым")
    private Long accountIdTo;

    @NotBlank(message = "Email обязательно должен быть указан")
    private String email;

    @Digits(integer = 10, fraction = 2, message = "Некорректный формат суммы")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @NotNull(message = "Сумма перевода не может быть пустой")
    private BigDecimal amount;
}
