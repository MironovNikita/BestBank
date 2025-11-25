package com.bank.common.mapper;

import com.bank.dto.account.RegisterAccountRequest;
import com.bank.entity.Account;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final SecureBase64Converter converter;
    private final PasswordEncoder encoder;

    public Account toAccount(RegisterAccountRequest request) {

        return new Account(
                null,
                converter.encrypt(request.getEmail().toLowerCase()),
                encoder.encode(request.getPassword()),
                request.getName(),
                request.getSurname(),
                request.getBirthdate(),
                request.getPhone(),
                0L);
    }

}
