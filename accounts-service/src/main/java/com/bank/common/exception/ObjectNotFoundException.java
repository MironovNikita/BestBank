package com.bank.common.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String object, Long id) {
        super(String.format("%s с ID: %s не найден!", object, id));
    }
}
