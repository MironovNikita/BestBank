package com.bank.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SecureBase64Converter {

    private static final String SECRET_KEY = "my-secret";

    public String encrypt(String attribute) {
        if (attribute == null || attribute.isEmpty()) return null;

        byte[] input = attribute.getBytes(StandardCharsets.UTF_8);
        byte[] key = getKeyBytes();
        byte[] result = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (input[i] ^ key[i % key.length]);
        }

        return Base64.getEncoder().encodeToString(result);
    }

    public String decrypt(String value) {
        if (value == null || value.isEmpty()) return null;

        byte[] decoded = Base64.getDecoder().decode(value);
        byte[] key = getKeyBytes();
        byte[] result = new byte[decoded.length];

        for (int i = 0; i < decoded.length; i++) {
            result[i] = (byte) (decoded[i] ^ key[i % key.length]);
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    private byte[] getKeyBytes() {
        return SECRET_KEY.getBytes(StandardCharsets.UTF_8);
    }
}
