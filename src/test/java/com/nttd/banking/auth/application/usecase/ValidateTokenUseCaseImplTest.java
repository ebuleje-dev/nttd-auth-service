package com.nttd.banking.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.exception.TokenExpiredException;
import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseImplTest {

  @Mock
  private TokenCacheRepository tokenCache;

  @Mock
  private JwtProvider jwtProvider;

  @InjectMocks
  private ValidateTokenUseCaseImpl validateTokenUseCase;

  @Test
  void whenValidateValidToken_thenReturnsJwtToken() {
    // Given
    String token = "valid.access.token";
    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .username("testuser")
        .roles(List.of("ROLE_CUSTOMER"))
        .userType("CUSTOMER")
        .tokenType("ACCESS")
        .issuedAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusDays(1))
        .build();

    when(jwtProvider.validateToken(token)).thenReturn(jwtToken);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(false));

    // When & Then
    StepVerifier.create(validateTokenUseCase.validate(token))
        .assertNext(result -> {
          assertNotNull(result);
          assertEquals("user123", result.getUserId());
          assertEquals("testuser", result.getUsername());
          assertEquals("CUSTOMER", result.getUserType());
        })
        .verifyComplete();
  }

  @Test
  void whenValidateBlacklistedToken_thenThrowsException() {
    // Given
    String token = "blacklisted.token";
    JwtToken jwtToken = JwtToken.builder()
        .jti("jti123")
        .userId("user123")
        .build();

    when(jwtProvider.validateToken(token)).thenReturn(jwtToken);
    when(tokenCache.isBlacklisted("jti123")).thenReturn(Mono.just(true));

    // When & Then
    StepVerifier.create(validateTokenUseCase.validate(token))
        .expectError(TokenExpiredException.class)
        .verify();
  }

  @Test
  void whenValidateInvalidToken_thenThrowsException() {
    // Given
    String invalidToken = "invalid.token";
    when(jwtProvider.validateToken(invalidToken))
        .thenThrow(new RuntimeException("Invalid token"));

    // When & Then
    StepVerifier.create(validateTokenUseCase.validate(invalidToken))
        .expectError(TokenExpiredException.class)
        .verify();
  }
}
