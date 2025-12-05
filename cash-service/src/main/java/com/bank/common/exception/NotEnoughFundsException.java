package com.bank.common.exception;

import java.math.BigDecimal;

public class NotEnoughFundsException extends RuntimeException {
    public NotEnoughFundsException(BigDecimal amount) {
        super("На вашем счёте недостаточно средств: " + amount);
    }
}
