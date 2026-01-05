package com.nttd.banking.auth.domain.port.in;

import com.nttd.banking.auth.domain.model.JwtToken;
import reactor.core.publisher.Mono;

/**
 * Use case for JWT token validation.
 */
public interface ValidateTokenUseCase {
  /**
   * Validates a JWT token.
   *
   * @param token JWT token string
   * @return Mono with token claims
   */
  Mono<JwtToken> validate(String token);
}
