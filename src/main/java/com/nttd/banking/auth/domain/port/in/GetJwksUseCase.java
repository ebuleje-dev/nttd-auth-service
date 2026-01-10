package com.nttd.banking.auth.domain.port.in;

import com.nttd.banking.auth.domain.model.Jwks;
import reactor.core.publisher.Mono;

/**
 * Use case for retrieving JWKS (JSON Web Key Set).
 * Provides the public key in JWKS format for external JWT validation.
 */
public interface GetJwksUseCase {
  /**
   * Retrieves the JWKS containing the public RSA key.
   *
   * @return Mono containing the JWKS
   */
  Mono<Jwks> getJwks();
}
