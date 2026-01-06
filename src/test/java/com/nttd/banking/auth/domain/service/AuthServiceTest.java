package com.nttd.banking.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.nttd.banking.auth.domain.port.in.RefreshTokenUseCase;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenCacheRepository tokenCache;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserEventPublisher eventPublisher;

  @InjectMocks
  private AuthService authService;

  private User testUser;
  private RegisterUseCase.RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id("user123")
        .username("testuser")
        .email("test@example.com")
        .passwordHash("$2a$12$hashedPassword")
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("+51987654321")
        .userType(UserType.CUSTOMER)
        .roles(List.of("ROLE_CUSTOMER"))
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    registerRequest = new RegisterUseCase.RegisterRequest(
        "testuser",
        "test@example.com",
        "Password123!",
        "DNI",
        "12345678",
        "+51987654321",
        "CUSTOMER"
    );
  }

  @Test
  void whenLoginWithValidCredentials_thenReturnsTokens() {
    // Given
    when(tokenCache.getLoginAttempts(anyString())).thenReturn(Mono.just(0L));
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(tokenCache.resetLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .username("testuser")
        .roles(List.of("ROLE_CUSTOMER"))
        .userType("CUSTOMER")
        .issuedAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusDays(1))
        .tokenType("ACCESS")
        .build();

    when(jwtService.generateAccessToken(any(User.class))).thenReturn(jwtToken);
    when(jwtService.generateAccessTokenString(any(User.class))).thenReturn("access.token.here");
    when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh.token.here");
    when(tokenCache.registerActiveToken(anyString(), anyString(), any(Duration.class)))
        .thenReturn(Mono.empty());
    when(eventPublisher.publishUserLogin(any(UserLoginEvent.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(authService.login("testuser", "Password123!"))
        .assertNext(result -> {
          assertNotNull(result);
          assertEquals("access.token.here", result.accessToken());
          assertEquals("refresh.token.here", result.refreshToken());
          assertEquals("testuser", result.username());
        })
        .verifyComplete();

    verify(eventPublisher).publishUserLogin(any(UserLoginEvent.class));
  }

  @Test
  void whenLoginWithInvalidPassword_thenThrowsException() {
    // Given
    when(tokenCache.getLoginAttempts(anyString())).thenReturn(Mono.just(0L));
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    when(tokenCache.incrementLoginAttempts(anyString())).thenReturn(Mono.just(1L));

    // When & Then
    StepVerifier.create(authService.login("testuser", "wrongpassword"))
        .expectError(InvalidCredentialsException.class)
        .verify();

    verify(tokenCache).incrementLoginAttempts("testuser");
  }

  @Test
  void whenLoginWithTooManyAttempts_thenThrowsException() {
    // Given
    when(tokenCache.getLoginAttempts(anyString())).thenReturn(Mono.just(5L));
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));

    // When & Then
    StepVerifier.create(authService.login("testuser", "Password123!"))
        .expectError(TooManyLoginAttemptsException.class)
        .verify();
  }

  @Test
  void whenRegisterValidUser_thenReturnsUser() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
    when(eventPublisher.publishUserRegistered(any(UserRegisteredEvent.class)))
        .thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(authService.register(registerRequest))
        .assertNext(user -> {
          assertNotNull(user);
          assertEquals("testuser", user.getUsername());
          assertEquals("test@example.com", user.getEmail());
        })
        .verifyComplete();

    verify(eventPublisher).publishUserRegistered(any(UserRegisteredEvent.class));
  }

  @Test
  void whenRegisterDuplicateUsername_thenThrowsException() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));

    // When & Then
    StepVerifier.create(authService.register(registerRequest))
        .expectError(UserAlreadyExistsException.class)
        .verify();
  }

  @Test
  void whenLogout_thenAddsTokenToBlacklist() {
    // Given
    String token = "valid.jwt.token";
    when(jwtService.extractJti(token)).thenReturn("jti123");
    when(tokenCache.addToBlacklist(anyString(), any(Duration.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(authService.logout(token))
        .verifyComplete();

    verify(tokenCache).addToBlacklist(eq("jti123"), any(Duration.class));
  }

  @Test
  void whenRefreshValidToken_thenReturnsNewAccessToken() {
    // Given
    String refreshToken = "valid.refresh.token";
    Claims claims = Jwts.claims()
        .subject("user123")
        .id("jti123")
        .add("tokenType", "REFRESH")
        .build();

    when(jwtService.validateAndGetClaims(refreshToken)).thenReturn(claims);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(false));
    when(userRepository.findById("user123")).thenReturn(Mono.just(testUser));
    when(jwtService.generateAccessTokenString(any(User.class)))
        .thenReturn("new.access.token");

    // When & Then
    StepVerifier.create(authService.refresh(refreshToken))
        .assertNext(result -> {
          assertNotNull(result);
          assertEquals("new.access.token", result.accessToken());
          assertTrue(result.expiresIn() > 0);
        })
        .verifyComplete();
  }

  @Test
  void whenRefreshBlacklistedToken_thenThrowsException() {
    // Given
    String refreshToken = "blacklisted.refresh.token";
    Claims claims = Jwts.claims()
        .subject("user123")
        .id("jti123")
        .build();

    when(jwtService.validateAndGetClaims(refreshToken)).thenReturn(claims);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(true));

    // When & Then
    StepVerifier.create(authService.refresh(refreshToken))
        .expectError(TokenExpiredException.class)
        .verify();
  }

  @Test
  void whenValidateValidToken_thenReturnsJwtToken() {
    // Given
    String token = "valid.access.token";
    Claims claims = Jwts.claims()
        .subject("user123")
        .id("jti123")
        .add("username", "testuser")
        .add("roles", List.of("ROLE_CUSTOMER"))
        .add("userType", "CUSTOMER")
        .add("tokenType", "ACCESS")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 86400000))
        .build();

    when(jwtService.validateAndGetClaims(token)).thenReturn(claims);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(false));

    // When & Then
    StepVerifier.create(authService.validate(token))
        .assertNext(jwtToken -> {
          assertNotNull(jwtToken);
          assertEquals("user123", jwtToken.getUserId());
          assertEquals("testuser", jwtToken.getUsername());
          assertEquals("CUSTOMER", jwtToken.getUserType());
        })
        .verifyComplete();
  }

  @Test
  void whenValidateBlacklistedToken_thenThrowsException() {
    // Given
    String token = "blacklisted.token";
    Claims claims = Jwts.claims()
        .id("jti123")
        .build();

    when(jwtService.validateAndGetClaims(token)).thenReturn(claims);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(true));

    // When & Then
    StepVerifier.create(authService.validate(token))
        .expectError(TokenExpiredException.class)
        .verify();
  }
}
