package com.nttd.banking.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.exception.InvalidCredentialsException;
import com.nttd.banking.auth.domain.exception.TokenExpiredException;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import com.nttd.banking.auth.domain.port.out.UserRepository;
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
class RefreshTokenUseCaseImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenCacheRepository tokenCache;

  @Mock
  private JwtProvider jwtProvider;

  @InjectMocks
  private RefreshTokenUseCaseImpl refreshTokenUseCase;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id("user123")
        .username("testuser")
        .email("test@example.com")
        .userType(UserType.CUSTOMER)
        .roles(List.of("ROLE_CUSTOMER"))
        .active(true)
        .build();
  }

  @Test
  void whenRefreshValidToken_thenReturnsNewAccessToken() {
    // Given
    String refreshToken = "valid.refresh.token";
    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .tokenType("REFRESH")
        .issuedAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusDays(7))
        .build();

    when(jwtProvider.validateToken(refreshToken)).thenReturn(jwtToken);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(false));
    when(userRepository.findById("user123")).thenReturn(Mono.just(testUser));
    when(jwtProvider.generateAccessTokenString(any(User.class)))
        .thenReturn("new.access.token");
    when(jwtProvider.getAccessTokenExpiration()).thenReturn(86400L);

    // When & Then
    StepVerifier.create(refreshTokenUseCase.refresh(refreshToken))
        .assertNext(result -> {
          assertNotNull(result);
          assertEquals("new.access.token", result.accessToken());
          assertEquals(86400L, result.expiresIn());
        })
        .verifyComplete();
  }

  @Test
  void whenRefreshBlacklistedToken_thenThrowsException() {
    // Given
    String refreshToken = "blacklisted.refresh.token";
    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .tokenType("REFRESH")
        .build();

    when(jwtProvider.validateToken(refreshToken)).thenReturn(jwtToken);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(true));

    // When & Then
    StepVerifier.create(refreshTokenUseCase.refresh(refreshToken))
        .expectError(TokenExpiredException.class)
        .verify();
  }

  @Test
  void whenRefreshWithUserNotFound_thenThrowsException() {
    // Given
    String refreshToken = "valid.refresh.token";
    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .tokenType("REFRESH")
        .build();

    when(jwtProvider.validateToken(refreshToken)).thenReturn(jwtToken);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(false));
    when(userRepository.findById("user123")).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(refreshTokenUseCase.refresh(refreshToken))
        .expectError(InvalidCredentialsException.class)
        .verify();
  }

  @Test
  void whenRefreshInvalidToken_thenThrowsException() {
    // Given
    String invalidToken = "invalid.token";
    when(jwtProvider.validateToken(invalidToken))
        .thenThrow(new RuntimeException("Invalid token"));

    // When & Then
    StepVerifier.create(refreshTokenUseCase.refresh(invalidToken))
        .expectError(TokenExpiredException.class)
        .verify();
  }
}
