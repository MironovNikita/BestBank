package com.bank.common.exception;

public class NotEnoughFundsException extends RuntimeException {
    public NotEnoughFundsException(Long amount) {
        super("На вашем счёте недостаточно средств: %d".formatted(amount));
    }
}
