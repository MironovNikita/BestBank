package com.bank.dto;

import com.bank.validation.Adult;
import com.bank.validation.Phone;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterAccountRequest {

    @Email(message = "Переданный email не соответствует формату.")
    @NotBlank(message = "Поле email должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер email должен составлять от 5 до 50 символов")
    private String email;

    @NotBlank(message = "Поле пароля должно быть заполнено.")
    @Size(min = 5, max = 50, message = "Размер пароля должен составлять от 5 до 50 символов")
    private String password;

    @NotBlank(message = "Поле имени должно быть заполнено.")
    @Size(min = 2, max = 30, message = "Размер имени должен составлять от 2 до 30 символов")
    private String name;

    @NotBlank(message = "Поле фамилии должно быть заполнено.")
    @Size(min = 2, max = 30, message = "Размер фамилии должен составлять от 2 до 30 символов")
    private String surname;

    @Adult
    @NotNull(message = "Дата рождения не должна быть пустой")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthdate;

    @Phone
    @NotBlank(message = "Поле номера телефона должно быть заполнено.")
    @Size(min = 11, max = 11, message = "Размер номера телефона должен составлять 11 символов")
    private String phone;
}
