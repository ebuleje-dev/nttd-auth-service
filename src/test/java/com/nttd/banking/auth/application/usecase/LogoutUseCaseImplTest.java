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
  void whenLogout_thenBlacklistsBothTokens() {
    // Given
    String token = "valid.jwt.token";
    String accessJti = "accessJti123";
    String refreshJti = "refreshJti456";

    when(jwtProvider.extractJti(token)).thenReturn(accessJti);
    when(jwtProvider.getAccessTokenExpiration()).thenReturn(86400L);
    when(tokenCache.addToBlacklist(eq(accessJti), any(Duration.class))).thenReturn(Mono.empty());
    when(tokenCache.getRefreshJtiByAccessJti(accessJti)).thenReturn(Mono.just(refreshJti));
    when(tokenCache.addToBlacklist(eq(refreshJti), any(Duration.class))).thenReturn(Mono.empty());
    when(tokenCache.removeTokenPair(accessJti)).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(logoutUseCase.logout(token))
        .verifyComplete();

    // Verify both tokens were blacklisted
    verify(tokenCache).addToBlacklist(eq(accessJti), any(Duration.class));
    verify(tokenCache).addToBlacklist(eq(refreshJti), any(Duration.class));
    verify(tokenCache).removeTokenPair(accessJti);
  }

  @Test
  void whenLogout_withNoRefreshToken_thenOnlyBlacklistsAccessToken() {
    // Given
    String token = "valid.jwt.token";
    String accessJti = "accessJti123";

    when(jwtProvider.extractJti(token)).thenReturn(accessJti);
    when(jwtProvider.getAccessTokenExpiration()).thenReturn(86400L);
    when(tokenCache.addToBlacklist(eq(accessJti), any(Duration.class))).thenReturn(Mono.empty());
    when(tokenCache.getRefreshJtiByAccessJti(accessJti)).thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(logoutUseCase.logout(token))
        .verifyComplete();

    // Verify only access token was blacklisted
    verify(tokenCache).addToBlacklist(eq(accessJti), any(Duration.class));
    verify(tokenCache, never()).removeTokenPair(anyString());
  }
}
