package com.bank.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    @Email(message = "Переданный email не соответствует формату.")
    @NotBlank(message = "Поле email должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер email должен составлять от 5 до 50 символов")
    private String email;

    @NotBlank(message = "Поле пароля должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер пароля должен составлять от 5 до 50 символов")
    private String password;
}
