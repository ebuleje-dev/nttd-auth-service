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

  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

  @Override
  public Mono<Void> logout(String token) {
    String accessJti = jwtProvider.extractJti(token);
    Duration accessTtl = Duration.ofSeconds(jwtProvider.getAccessTokenExpiration());

    log.debug("Processing logout for accessJti: {}", accessJti);

    // Blacklist the access token
    return tokenCache.addToBlacklist(accessJti, accessTtl)
        // Find and blacklist the associated refresh token
        .then(tokenCache.getRefreshJtiByAccessJti(accessJti))
        .flatMap(refreshJti -> {
          log.debug("Found associated refreshJti: {}, adding to blacklist", refreshJti);
          return tokenCache.addToBlacklist(refreshJti, REFRESH_TOKEN_TTL)
              .then(tokenCache.removeTokenPair(accessJti));
        })
        // If no refresh token found, just complete (token pair may have expired)
        .onErrorResume(e -> {
          log.warn("Could not blacklist refresh token: {}", e.getMessage());
          return Mono.empty();
        })
        .then();
  }
}
