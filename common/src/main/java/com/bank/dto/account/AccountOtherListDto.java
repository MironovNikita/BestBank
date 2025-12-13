package com.bank.dto.account;

import com.bank.dto.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountOtherListDto {
    private Long id;
    private Long ownerId;
    private Currency currency;
    private String name;
    private String surname;
    private String phone;
}
