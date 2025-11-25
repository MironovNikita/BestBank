package com.bank.common.exception;

public class AccountEditException extends RuntimeException {
  public AccountEditException() {
    super("Введённые email/номер телефона уже существуют!");
  }
}
