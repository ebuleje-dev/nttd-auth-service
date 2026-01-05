package com.nttd.banking.auth.domain.port.in;

import reactor.core.publisher.Mono;

/**
 * Use case for user logout.
 */
public interface LogoutUseCase {
  /**
   * Logs out a user (adds token to blacklist).
   *
   * @param token JWT token to invalidate
   * @return Mono completion signal
   */
  Mono<Void> logout(String token);
}
