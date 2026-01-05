package com.nttd.banking.auth.application.exception;

/**
 * Exception thrown when user exceeds maximum login attempts.
 */
public class TooManyLoginAttemptsException extends RuntimeException {
  public TooManyLoginAttemptsException(String message) {
    super(message);
  }
}
