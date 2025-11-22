package com.bank.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailNotificationDto {

    @Email(message = "Email для отправки уведомления не соответствует формату")
    @NotBlank(message = "Email для отправки уведомления не может быть пустым")
    private String to;

    @NotBlank(message = "Тема уведомления не может быть пустой")
    private String subject;

    @NotBlank(message = "Текст уведомления не может быть пустым")
    private String text;
}
