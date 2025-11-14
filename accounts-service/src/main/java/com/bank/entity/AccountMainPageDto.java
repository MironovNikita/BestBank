package com.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountMainPageDto {
    private String phone;
    private String name;
    private String surname;
}
