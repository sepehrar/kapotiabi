package com.kapotiabi.exception;

public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException() {
    super("Email already registered");
  }
}
