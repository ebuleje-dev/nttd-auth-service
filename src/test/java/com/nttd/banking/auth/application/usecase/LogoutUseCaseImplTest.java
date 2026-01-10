package com.nttd.banking.auth.application.usecase;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.port.out.JwtProvider;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

  @Mock
  private TokenCacheRepository tokenCache;

  @Mock
  private JwtProvider jwtProvider;

  @InjectMocks
  private LogoutUseCaseImpl logoutUseCase;

  @Test
  void whenLogout_thenAddsTokenToBlacklist() {
    // Given
    String token = "valid.jwt.token";
    when(jwtProvider.extractJti(token)).thenReturn("jti123");
    when(jwtProvider.getAccessTokenExpiration()).thenReturn(86400L);
    when(tokenCache.addToBlacklist(anyString(), any(Duration.class))).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(logoutUseCase.logout(token))
        .verifyComplete();

    verify(tokenCache).addToBlacklist(eq("jti123"), any(Duration.class));
  }
}
