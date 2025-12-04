package com.bank.common.exception;

public class UserEditException extends RuntimeException {
  public UserEditException() {
    super("Введённые email/номер телефона уже существуют!");
  }
}
