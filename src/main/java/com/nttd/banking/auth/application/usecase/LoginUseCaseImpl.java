package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.event.UserLoginEvent;
import com.nttd.banking.auth.domain.exception.InvalidCredentialsException;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.port.in.LoginUseCase;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import com.nttd.banking.auth.domain.service.AuthDomainService;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the login use case.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class LoginUseCaseImpl implements LoginUseCase {

  private final UserRepository userRepository;
  private final TokenCacheRepository tokenCache;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final UserEventPublisher eventPublisher;
  private final AuthDomainService authDomainService;

  @Override
  public Mono<LoginResult> login(String username, String password) {
    return authDomainService.checkLoginAttempts(username)
        .then(userRepository.findByUsername(username))
        .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
        .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
        .switchIfEmpty(Mono.defer(() -> {
          tokenCache.incrementLoginAttempts(username).subscribe();
          return Mono.error(new InvalidCredentialsException("Invalid credentials"));
        }))
        .filter(User::getActive)
        .switchIfEmpty(Mono.error(new InvalidCredentialsException("User is inactive")))
        .flatMap(user -> {
          // Reset login attempts on successful login
          User updatedUser = authDomainService.updateLastLogin(user);
          return tokenCache.resetLoginAttempts(username)
              .then(userRepository.save(updatedUser))
              .flatMap(this::generateTokens);
        });
  }

  /**
   * Generates access and refresh tokens.
   */
  private Mono<LoginResult> generateTokens(User user) {
    JwtToken accessTokenMetadata = jwtProvider.generateAccessToken(user);
    String accessToken = jwtProvider.generateAccessTokenString(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    // Register active token in Redis
    Duration ttl = Duration.ofSeconds(jwtProvider.getAccessTokenExpiration());
    return tokenCache.registerActiveToken(user.getId(), accessTokenMetadata.getJti(), ttl)
        .then(Mono.defer(() -> {
          LoginResult loginResult = new LoginResult(
              accessToken,
              refreshToken,
              ttl.getSeconds(),
              user.getId(),
              user.getUsername(),
              user.getRoles(),
              user.getUserType().name()
          );

          UserLoginEvent event = UserLoginEvent.builder()
              .userId(user.getId())
              .username(user.getUsername())
              .loginAt(LocalDateTime.now())
              .build();

          return eventPublisher.publishUserLogin(event)
              .thenReturn(loginResult);
        }));
  }
}
