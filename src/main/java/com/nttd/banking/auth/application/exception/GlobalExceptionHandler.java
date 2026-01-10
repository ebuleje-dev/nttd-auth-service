package com.nttd.banking.auth.application.exception;

import com.nttd.banking.auth.domain.exception.InvalidCredentialsException;
import com.nttd.banking.auth.domain.exception.TokenExpiredException;
import com.nttd.banking.auth.domain.exception.TooManyLoginAttemptsException;
import com.nttd.banking.auth.domain.exception.UserAlreadyExistsException;
import com.nttd.banking.auth.model.dto.ErrorResponse;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for auth-service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles invalid credentials exception.
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentials(
      InvalidCredentialsException ex) {
    log.error("Invalid credentials: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.UNAUTHORIZED.value());
    error.setError("Unauthorized");
    error.setMessage(ex.getMessage());

    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
  }

  /**
   * Handles user already exists exception.
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyExists(
      UserAlreadyExistsException ex) {
    log.error("User already exists: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.CONFLICT.value());
    error.setError("Conflict");
    error.setMessage(ex.getMessage());

    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
  }

  /**
   * Handles too many login attempts exception.
   */
  @ExceptionHandler(TooManyLoginAttemptsException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleTooManyLoginAttempts(
      TooManyLoginAttemptsException ex) {
    log.error("Too many login attempts: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    error.setError("Too Many Requests");
    error.setMessage(ex.getMessage());

    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error));
  }

  /**
   * Handles token expired exception.
   */
  @ExceptionHandler(TokenExpiredException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleTokenExpired(
      TokenExpiredException ex) {
    log.error("Token expired: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.UNAUTHORIZED.value());
    error.setError("Unauthorized");
    error.setMessage(ex.getMessage());

    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
  }

  /**
   * Handles validation errors exception.
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(
      WebExchangeBindException ex) {
    log.error("Validation error: {}", ex.getMessage());

    String validationMessage = "Validation failed";
    if (ex.getFieldError() != null && ex.getFieldError().getDefaultMessage() != null) {
      validationMessage = "Validation failed: " + ex.getFieldError().getDefaultMessage();
    }

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.BAD_REQUEST.value());
    error.setError("Bad Request");
    error.setMessage(validationMessage);

    return Mono.just(ResponseEntity.badRequest().body(error));
  }

  /**
   * Handles generic unexpected errors.
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<ErrorResponse>> handleGenericError(Exception ex) {
    log.error("Unexpected error", ex);

    ErrorResponse error = new ErrorResponse();
    error.setTimestamp(OffsetDateTime.now());
    error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.setError("Internal Server Error");
    error.setMessage("An unexpected error occurred");

    return Mono.just(ResponseEntity.internalServerError().body(error));
  }
}
