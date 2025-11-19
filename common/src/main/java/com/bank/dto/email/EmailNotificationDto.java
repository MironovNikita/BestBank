package com.bank.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailNotificationDto {
    private String to;
    private String subject;
    private String text;
}
