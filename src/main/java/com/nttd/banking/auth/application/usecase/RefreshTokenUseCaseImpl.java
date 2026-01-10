package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.exception.InvalidCredentialsException;
import com.nttd.banking.auth.domain.exception.TokenExpiredException;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.port.in.RefreshTokenUseCase;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the refresh token use case.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

  private final UserRepository userRepository;
  private final TokenCacheRepository tokenCache;
  private final JwtProvider jwtProvider;

  @Override
  public Mono<RefreshResult> refresh(String refreshToken) {
    return Mono.fromCallable(() -> jwtProvider.validateToken(refreshToken))
        .flatMap(jwtToken -> {
          String userId = jwtToken.getUserId();
          String jti = jwtToken.getJti();

          // Verify not blacklisted
          return tokenCache.isBlacklisted(jti)
              .flatMap(isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                  return Mono.error(new TokenExpiredException("Refresh token revoked"));
                }

                // Get user and generate new access token
                return userRepository.findById(userId)
                    .switchIfEmpty(Mono.error(
                        new InvalidCredentialsException("User not found")))
                    .flatMap(user -> {
                      String newAccessToken = jwtProvider.generateAccessTokenString(user);
                      long expiresIn = jwtProvider.getAccessTokenExpiration();

                      return Mono.just(new RefreshResult(
                          newAccessToken,
                          expiresIn
                      ));
                    });
              });
        })
        .onErrorMap(e -> {
          if (e instanceof TokenExpiredException || e instanceof InvalidCredentialsException) {
            return e;
          }
          return new TokenExpiredException("Invalid or expired refresh token");
        });
  }
}
