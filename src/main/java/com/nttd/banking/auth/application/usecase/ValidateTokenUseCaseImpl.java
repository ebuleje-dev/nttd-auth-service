package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.exception.TokenExpiredException;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.port.in.ValidateTokenUseCase;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the validate token use case.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ValidateTokenUseCaseImpl implements ValidateTokenUseCase {

  private final TokenCacheRepository tokenCache;
  private final JwtProvider jwtProvider;

  @Override
  public Mono<JwtToken> validate(String token) {
    return Mono.fromCallable(() -> jwtProvider.validateToken(token))
        .flatMap(jwtToken -> {
          String jti = jwtToken.getJti();

          // Verify not blacklisted
          return tokenCache.isBlacklisted(jti)
              .flatMap(isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                  return Mono.error(new TokenExpiredException("Token revoked"));
                }

                return Mono.just(jwtToken);
              });
        })
        .onErrorMap(e -> {
          if (e instanceof TokenExpiredException) {
            return e;
          }
          return new TokenExpiredException("Invalid or expired token");
        });
  }
}
