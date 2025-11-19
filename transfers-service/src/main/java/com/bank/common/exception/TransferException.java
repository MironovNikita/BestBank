package com.bank.common.exception;

public class TransferException extends RuntimeException {
    public TransferException() {
        super("Недостаточно средств или счёт не найден!");
    }
}
