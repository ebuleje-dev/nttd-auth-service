package com.nttd.banking.auth.domain.exception;

/**
 * Exception thrown when a JWT token has expired.
 */
public class TokenExpiredException extends RuntimeException {
  public TokenExpiredException(String message) {
    super(message);
  }
}
