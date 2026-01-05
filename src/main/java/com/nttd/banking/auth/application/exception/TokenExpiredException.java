package com.nttd.banking.auth.application.exception;

/**
 * Exception thrown when a JWT token has expired.
 */
public class TokenExpiredException extends RuntimeException {
  public TokenExpiredException(String message) {
    super(message);
  }
}
