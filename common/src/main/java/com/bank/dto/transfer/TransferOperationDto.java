package com.bank.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

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

    @Positive(message = "Сумма перевода не может быть отрицательной или 0")
    @NotNull(message = "Сумма перевода не может быть пустой")
    private Long amount;
}
