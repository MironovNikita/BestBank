package com.bank.common.exception;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String email, String message) {
        super("При регистрации пользователя с email %s указаны уже существующие параметры: %s".formatted(email, message));
    }
}
