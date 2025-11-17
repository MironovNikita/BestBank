package com.bank.common.exception;

public class LoginException extends RuntimeException {
    public LoginException() {
        super("Ошибка авторизации. Введён неверный логин/пароль.");
    }
}
