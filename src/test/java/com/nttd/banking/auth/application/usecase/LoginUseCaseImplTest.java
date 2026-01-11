package com.nttd.banking.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.event.UserLoginEvent;
import com.nttd.banking.auth.domain.exception.InvalidCredentialsException;
import com.nttd.banking.auth.domain.exception.TooManyLoginAttemptsException;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import com.nttd.banking.auth.domain.service.AuthDomainService;
import java.time.Duration;
import java.time.LocalDateTime;
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
class LoginUseCaseImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenCacheRepository tokenCache;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private UserEventPublisher eventPublisher;

  @Mock
  private AuthDomainService authDomainService;

  @InjectMocks
  private LoginUseCaseImpl loginUseCase;

  private User testUser;

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
  }

  @Test
  void whenLoginWithValidCredentials_thenReturnsTokens() {
    // Given
    when(authDomainService.checkLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(authDomainService.updateLastLogin(any(User.class))).thenReturn(testUser);
    when(tokenCache.resetLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

    when(jwtProvider.generateAccessTokenString(any(User.class))).thenReturn("access.token.here");
    when(jwtProvider.generateRefreshToken(any(User.class))).thenReturn("refresh.token.here");
    when(jwtProvider.extractJti("access.token.here")).thenReturn("accessJti123");
    when(jwtProvider.extractJti("refresh.token.here")).thenReturn("refreshJti456");
    when(jwtProvider.getAccessTokenExpiration()).thenReturn(86400L);
    when(tokenCache.registerActiveToken(anyString(), anyString(), any(Duration.class)))
        .thenReturn(Mono.empty());
    when(tokenCache.saveTokenPair(anyString(), anyString(), any(Duration.class)))
        .thenReturn(Mono.empty());
    when(eventPublisher.publishUserLogin(any(UserLoginEvent.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(loginUseCase.login("testuser", "Password123!"))
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
    when(authDomainService.checkLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    when(tokenCache.incrementLoginAttempts(anyString())).thenReturn(Mono.just(1L));

    // When & Then
    StepVerifier.create(loginUseCase.login("testuser", "wrongpassword"))
        .expectError(InvalidCredentialsException.class)
        .verify();

    verify(tokenCache).incrementLoginAttempts("testuser");
  }

  @Test
  void whenLoginWithTooManyAttempts_thenThrowsException() {
    // Given - Mock needed due to eager evaluation of .then() arguments
    when(authDomainService.checkLoginAttempts(anyString()))
        .thenReturn(Mono.error(new TooManyLoginAttemptsException("Too many login attempts")));
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));

    // When & Then
    StepVerifier.create(loginUseCase.login("testuser", "Password123!"))
        .expectError(TooManyLoginAttemptsException.class)
        .verify();
  }

  @Test
  void whenLoginWithUserNotFound_thenThrowsException() {
    // Given
    when(authDomainService.checkLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(loginUseCase.login("testuser", "Password123!"))
        .expectError(InvalidCredentialsException.class)
        .verify();
  }

  @Test
  void whenLoginWithInactiveUser_thenThrowsException() {
    // Given
    User inactiveUser = User.builder()
        .id("user123")
        .username("testuser")
        .passwordHash("$2a$12$hashedPassword")
        .userType(UserType.CUSTOMER)
        .active(false)
        .build();

    when(authDomainService.checkLoginAttempts(anyString())).thenReturn(Mono.empty());
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(inactiveUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    // When & Then
    StepVerifier.create(loginUseCase.login("testuser", "Password123!"))
        .expectError(InvalidCredentialsException.class)
        .verify();
  }
}
