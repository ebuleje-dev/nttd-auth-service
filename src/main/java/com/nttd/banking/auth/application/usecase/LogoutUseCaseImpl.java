package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.port.in.LogoutUseCase;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the logout use case.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCaseImpl implements LogoutUseCase {

  private final TokenCacheRepository tokenCache;
  private final JwtProvider jwtProvider;

  @Override
  public Mono<Void> logout(String token) {
    String jti = jwtProvider.extractJti(token);
    // Calculate remaining TTL of the token
    Duration ttl = Duration.ofSeconds(jwtProvider.getAccessTokenExpiration());
    log.debug("Adding token {} to blacklist with TTL: {}", jti, ttl);
    return tokenCache.addToBlacklist(jti, ttl);
  }
}
