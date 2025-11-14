package com.bank.dto;

import com.bank.ServiceResponse;

import java.util.List;

import static com.bank.ModulesEnum.ACCOUNTS_SERVICE;

public class AccountServiceResponse extends ServiceResponse {

    public AccountServiceResponse(Integer code, boolean success, List<String> errors) {
        super(ACCOUNTS_SERVICE.name(), code, success, errors);
    }
}
