package com.nttd.banking.auth.domain.port.in;

import reactor.core.publisher.Mono;

/**
 * Use case for refreshing JWT tokens.
 */
public interface RefreshTokenUseCase {
  /**
   * Generates a new access token using refresh token.
   *
   * @param refreshToken Refresh token
   * @return Mono with new access token
   */
  Mono<RefreshResult> refresh(String refreshToken);

  /**
   * Refresh result.
   */
  record RefreshResult(
      String accessToken,
      Long expiresIn
  ) {}
}
