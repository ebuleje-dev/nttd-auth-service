package com.nttd.banking.auth.domain.port.out;

import java.time.Duration;
import reactor.core.publisher.Mono;

/**
 * Token cache repository port (Redis).
 */
public interface TokenCacheRepository {
  Mono<Void> addToBlacklist(String jti, Duration ttl);

  Mono<Boolean> isBlacklisted(String jti);

  Mono<Void> registerActiveToken(String userId, String jti, Duration ttl);

  Mono<Void> removeActiveToken(String userId, String jti);

  Mono<Long> incrementLoginAttempts(String username);

  Mono<Void> resetLoginAttempts(String username);

  Mono<Long> getLoginAttempts(String username);

  /**
   * Stores the relationship between access token JTI and refresh token JTI.
   *
   * @param accessJti the access token JTI
   * @param refreshJti the refresh token JTI
   * @param ttl time to live (should match refresh token expiration)
   * @return Mono completion signal
   */
  Mono<Void> saveTokenPair(String accessJti, String refreshJti, Duration ttl);

  /**
   * Retrieves the refresh token JTI associated with an access token JTI.
   *
   * @param accessJti the access token JTI
   * @return Mono with the refresh token JTI, or empty if not found
   */
  Mono<String> getRefreshJtiByAccessJti(String accessJti);

  /**
   * Removes the token pair relationship.
   *
   * @param accessJti the access token JTI
   * @return Mono completion signal
   */
  Mono<Void> removeTokenPair(String accessJti);
}
