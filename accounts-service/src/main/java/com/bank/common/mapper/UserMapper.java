package com.bank.common.mapper;

import com.bank.dto.user.RegisterUserRequest;
import com.bank.entity.User;
import com.bank.security.SecureBase64Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final SecureBase64Converter converter;
    private final PasswordEncoder encoder;

    public User toUser(RegisterUserRequest rq) {

        return new User(
                null,
                converter.encrypt(rq.getEmail().toLowerCase()),
                encoder.encode(rq.getPassword()),
                rq.getName(),
                rq.getSurname(),
                rq.getBirthdate(),
                rq.getPhone()
        );
    }
}
