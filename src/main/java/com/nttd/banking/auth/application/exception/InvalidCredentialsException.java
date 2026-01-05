package com.nttd.banking.auth.application.exception;

/**
 * Exception thrown when user provides invalid credentials.
 */
public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException(String message) {
    super(message);
  }
}
