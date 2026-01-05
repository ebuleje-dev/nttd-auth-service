package com.nttd.banking.auth.domain.port.in;

import reactor.core.publisher.Mono;

/**
 * Use case for user login.
 */
public interface LoginUseCase {
  /**
   * Authenticates a user and generates JWT tokens.
   *
   * @param username Username or email
   * @param password Plain text password
   * @return Mono with access and refresh tokens
   */
  Mono<LoginResult> login(String username, String password);

  /**
   * Login result containing tokens and user info.
   */
  record LoginResult(
      String accessToken,
      String refreshToken,
      Long expiresIn,
      String userId,
      String username,
      java.util.List<String> roles,
      String userType
  ) {}
}
