package com.nttd.banking.auth.domain.service;

import com.nttd.banking.auth.application.exception.InvalidCredentialsException;
import com.nttd.banking.auth.application.exception.TokenExpiredException;
import com.nttd.banking.auth.application.exception.TooManyLoginAttemptsException;
import com.nttd.banking.auth.application.exception.UserAlreadyExistsException;
import com.nttd.banking.auth.domain.event.UserLoginEvent;
import com.nttd.banking.auth.domain.event.UserRegisteredEvent;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.in.LoginUseCase;
import com.nttd.banking.auth.domain.port.in.LogoutUseCase;
import com.nttd.banking.auth.domain.port.in.RefreshTokenUseCase;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.domain.port.in.ValidateTokenUseCase;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Authentication service implementing use cases.
 * Only loads when not in test profile.
 */
@Service
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class AuthService implements LoginUseCase, RegisterUseCase, LogoutUseCase,
    RefreshTokenUseCase, ValidateTokenUseCase {

  private final UserRepository userRepository;
  private final TokenCacheRepository tokenCache;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserEventPublisher eventPublisher;

  private static final int MAX_LOGIN_ATTEMPTS = 5;

  @Override
  public Mono<LoginResult> login(String username, String password) {
    return checkLoginAttempts(username)
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
          return tokenCache.resetLoginAttempts(username)
              .then(updateLastLogin(user))
              .flatMap(this::generateTokens);
        });
  }

  @Override
  public Mono<User> register(RegisterRequest request) {
    return validateUniqueUser(request)
        .then(Mono.defer(() -> {
          User user = User.builder()
              .username(request.username())
              .email(request.email())
              .passwordHash(passwordEncoder.encode(request.password()))
              .documentType(request.documentType())
              .documentNumber(request.documentNumber())
              .phoneNumber(request.phoneNumber())
              .userType(UserType.valueOf(request.userType()))
              .roles(getDefaultRoles(request.userType()))
              .active(true)
              .createdAt(LocalDateTime.now())
              .updatedAt(LocalDateTime.now())
              .build();

          return userRepository.save(user);
        }))
        .flatMap(savedUser -> {
          UserRegisteredEvent event = UserRegisteredEvent.builder()
              .userId(savedUser.getId())
              .username(savedUser.getUsername())
              .email(savedUser.getEmail())
              .documentType(savedUser.getDocumentType())
              .documentNumber(savedUser.getDocumentNumber())
              .phoneNumber(savedUser.getPhoneNumber())
              .userType(savedUser.getUserType().name())
              .registeredAt(savedUser.getCreatedAt())
              .build();

          return eventPublisher.publishUserRegistered(event)
              .thenReturn(savedUser);
        });
  }

  @Override
  public Mono<Void> logout(String token) {
    String jti = jwtService.extractJti(token);
    // Calculate remaining TTL of the token
    Duration ttl = Duration.ofHours(24); // TODO: calculate from token expiration
    return tokenCache.addToBlacklist(jti, ttl);
  }

  /**
   * Checks if user has exceeded max login attempts.
   */
  private Mono<Void> checkLoginAttempts(String username) {
    return tokenCache.getLoginAttempts(username)
        .flatMap(attempts -> {
          if (attempts >= MAX_LOGIN_ATTEMPTS) {
            return Mono.error(new TooManyLoginAttemptsException(
                "Too many login attempts. Try again later."));
          }
          return Mono.empty();
        });
  }

  /**
   * Validates that username, email and document number are unique.
   */
  private Mono<Void> validateUniqueUser(RegisterRequest request) {
    return userRepository.existsByUsername(request.username())
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Username already exists"))
            : Mono.empty())
        .then(userRepository.existsByEmail(request.email()))
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Email already exists"))
            : Mono.empty())
        .then(userRepository.existsByDocumentNumber(request.documentNumber()))
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Document number already exists"))
            : Mono.empty())
        .then();
  }

  /**
   * Updates user's last login timestamp.
   */
  private Mono<User> updateLastLogin(User user) {
    User updated = User.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .passwordHash(user.getPasswordHash())
        .documentType(user.getDocumentType())
        .documentNumber(user.getDocumentNumber())
        .phoneNumber(user.getPhoneNumber())
        .userType(user.getUserType())
        .customerId(user.getCustomerId())
        .roles(user.getRoles())
        .active(user.getActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(LocalDateTime.now())
        .lastLogin(LocalDateTime.now())
        .build();

    return userRepository.save(updated);
  }

  /**
   * Generates access and refresh tokens.
   */
  private Mono<LoginResult> generateTokens(User user) {
    JwtToken accessTokenMetadata = jwtService.generateAccessToken(user);
    String accessToken = jwtService.generateAccessTokenString(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // Register active token in Redis
    Duration ttl = Duration.ofHours(24);
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

  /**
   * Returns default roles based on user type.
   */
  private List<String> getDefaultRoles(String userType) {
    return switch (userType) {
      case "ADMIN" -> List.of("ROLE_ADMIN");
      case "EMPLOYEE" -> List.of("ROLE_EMPLOYEE");
      case "CUSTOMER" -> List.of("ROLE_CUSTOMER");
      case "YANKI_USER" -> List.of("ROLE_YANKI_USER");
      case "BOOTCOIN_USER" -> List.of("ROLE_BOOTCOIN_USER");
      default -> List.of();
    };
  }

  @Override
  public Mono<RefreshResult> refresh(String refreshToken) {
    return Mono.fromCallable(() -> jwtService.validateAndGetClaims(refreshToken))
        .flatMap(claims -> {
          String userId = claims.getSubject();
          String jti = claims.getId();

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
                      String newAccessToken = jwtService.generateAccessTokenString(user);
                      Duration ttl = Duration.ofHours(24);

                      return Mono.just(new RefreshResult(
                          newAccessToken,
                          ttl.getSeconds()
                      ));
                    });
              });
        })
        .onErrorMap(io.jsonwebtoken.JwtException.class, e ->
            new TokenExpiredException("Invalid or expired refresh token"));
  }

  @Override
  public Mono<JwtToken> validate(String token) {
    return Mono.fromCallable(() -> jwtService.validateAndGetClaims(token))
        .flatMap(claims -> {
          String jti = claims.getId();

          // Verify not blacklisted
          return tokenCache.isBlacklisted(jti)
              .flatMap(isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                  return Mono.error(new TokenExpiredException("Token revoked"));
                }

                JwtToken jwtToken = JwtToken.builder()
                    .jti(jti)
                    .userId(claims.getSubject())
                    .username(claims.get("username", String.class))
                    .roles((List<String>) claims.get("roles"))
                    .userType(claims.get("userType", String.class))
                    .issuedAt(toLocalDateTime(claims.getIssuedAt()))
                    .expiresAt(toLocalDateTime(claims.getExpiration()))
                    .tokenType(claims.get("tokenType", String.class))
                    .build();

                return Mono.just(jwtToken);
              });
        })
        .onErrorMap(io.jsonwebtoken.JwtException.class, e ->
            new TokenExpiredException("Invalid or expired token"));
  }

  /**
   * Converts Date to LocalDateTime.
   */
  private LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
