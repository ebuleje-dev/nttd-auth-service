package com.nttd.banking.auth.infrastructure.adapter.out.cache;

import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Redis adapter for token caching operations.
 * Only loads when not in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class TokenRedisAdapter implements TokenCacheRepository {

  private final ReactiveRedisTemplate<String, String> redisTemplate;

  private static final String BLACKLIST_PREFIX = "token:blacklist:";
  private static final String ACTIVE_PREFIX = "token:active:";
  private static final String LOGIN_ATTEMPTS_PREFIX = "user:login-attempts:";

  @Override
  public Mono<Void> addToBlacklist(String jti, Duration ttl) {
    String key = BLACKLIST_PREFIX + jti;
    return redisTemplate.opsForValue()
        .set(key, "revoked", ttl)
        .doOnSuccess(v -> log.debug("Token {} added to blacklist with TTL {}", jti, ttl))
        .then();
  }

  @Override
  public Mono<Boolean> isBlacklisted(String jti) {
    String key = BLACKLIST_PREFIX + jti;
    return redisTemplate.hasKey(key);
  }

  @Override
  public Mono<Void> registerActiveToken(String userId, String jti, Duration ttl) {
    String key = ACTIVE_PREFIX + userId + ":" + jti;
    return redisTemplate.opsForValue()
        .set(key, "active", ttl)
        .then();
  }

  @Override
  public Mono<Void> removeActiveToken(String userId, String jti) {
    String key = ACTIVE_PREFIX + userId + ":" + jti;
    return redisTemplate.delete(key).then();
  }

  @Override
  public Mono<Long> incrementLoginAttempts(String username) {
    String key = LOGIN_ATTEMPTS_PREFIX + username;
    return redisTemplate.opsForValue()
        .increment(key)
        .flatMap(count -> {
          if (count == 1) {
            return redisTemplate.expire(key, Duration.ofMinutes(15))
                .thenReturn(count);
          }
          return Mono.just(count);
        });
  }

  @Override
  public Mono<Void> resetLoginAttempts(String username) {
    String key = LOGIN_ATTEMPTS_PREFIX + username;
    return redisTemplate.delete(key).then();
  }

  @Override
  public Mono<Long> getLoginAttempts(String username) {
    String key = LOGIN_ATTEMPTS_PREFIX + username;
    return redisTemplate.opsForValue()
        .get(key)
        .map(Long::parseLong)
        .defaultIfEmpty(0L);
  }
}
