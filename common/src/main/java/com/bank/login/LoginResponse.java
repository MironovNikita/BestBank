package com.bank.login;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginResponse {
    private Long id;
    private String email;
    private String name;
}
