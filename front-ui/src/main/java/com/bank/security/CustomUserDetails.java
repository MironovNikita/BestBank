package com.bank.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements Principal {

    private final Long userId;
    private final String email;
    private final String name;

    @Override
    public String getName() { return email; }

    public Collection<GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
