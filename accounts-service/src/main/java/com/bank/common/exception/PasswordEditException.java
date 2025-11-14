package com.bank.common.exception;

public class PasswordEditException extends RuntimeException {
    public PasswordEditException() {
        super("Введённые пароли не совпадают.");
    }
}
