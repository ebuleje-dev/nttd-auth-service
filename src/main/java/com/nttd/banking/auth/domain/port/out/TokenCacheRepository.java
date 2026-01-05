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
}
