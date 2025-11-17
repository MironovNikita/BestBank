package com.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountPasswordChangeDto {
    @Size(min = 5, max = 50, message = "Размер пароля должен составлять от 5 до 50 символов")
    @NotBlank(message = "Поле пароля должно быть заполнено.")
    private String newPassword;

    @NotBlank(message = "Поле пароля должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер пароля должен составлять от 5 до 50 символов")
    private String confirmPassword;
}
