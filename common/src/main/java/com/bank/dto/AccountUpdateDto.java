package com.bank.dto;

import com.bank.validation.Adult;
import com.bank.validation.Phone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountUpdateDto {
    @Email(message = "Переданный email не соответствует формату.")
    @Size(min = 5, max = 50, message = "Размер email должен составлять от 5 до 50 символов")
    private String email;

    @Size(min = 2, max = 30, message = "Размер имени должен составлять от 2 до 30 символов")
    private String name;

    @Size(min = 2, max = 30, message = "Размер фамилии должен составлять от 2 до 30 символов")
    private String surname;

    @Adult
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthdate;

    @Phone
    @Size(min = 11, max = 11, message = "Размер номера телефона должен составлять 11 символов")
    private String phone;

    public void setEmail(String email) {
        this.email = normalize(email);
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public void setSurname(String surname) {
        this.surname = normalize(surname);
    }

    public void setPhone(String phone) {
        this.phone = normalize(phone);
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
