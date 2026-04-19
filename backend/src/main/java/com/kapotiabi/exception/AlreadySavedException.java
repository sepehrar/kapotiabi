package com.kapotiabi.exception;

public class AlreadySavedException extends RuntimeException {

  public AlreadySavedException() {
    super("Shop already saved");
  }
}
