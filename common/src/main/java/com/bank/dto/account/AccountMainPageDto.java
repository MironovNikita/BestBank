package com.bank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountMainPageDto {
    private String id;
    private String phone;
    private String name;
    private String surname;
}
