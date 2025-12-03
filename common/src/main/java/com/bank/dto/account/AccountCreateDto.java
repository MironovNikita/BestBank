package com.bank.dto.account;

import com.bank.dto.currency.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountCreateDto {

    @NotBlank(message = "Название счёта не должно быть пустым.")
    @Size(min = 1, max = 50, message = "Название счёта должно содержать от 1 до 50 символов")
    private String title;

    @NotNull(message = "Необходимо указать валюту счёта.")
    private Currency currency;

    @NotBlank(message = "Поле email должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер email должен составлять от 5 до 50 символов")
    private String email;
}
