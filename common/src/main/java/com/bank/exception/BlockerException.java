package com.bank.exception;

public class BlockerException extends RuntimeException {
    public BlockerException() {
        super("Подозрительная операция была заблокирована. Обратитесь в банк");
    }
}
