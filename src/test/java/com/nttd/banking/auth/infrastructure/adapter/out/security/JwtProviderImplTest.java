package com.nttd.banking.auth.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtProviderImplTest {

  private JwtProviderImpl jwtProvider;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private User testUser;

  @BeforeEach
  void setUp() throws Exception {
    // Generate RSA key pair for testing
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();
    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();

    jwtProvider = new JwtProviderImpl(
        privateKey,
        publicKey,
        86400000L, // 24 hours
        604800000L  // 7 days
    );

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
  void whenGenerateAccessToken_thenReturnsValidMetadata() {
    // When
    JwtToken token = jwtProvider.generateAccessToken(testUser);

    // Then
    assertNotNull(token);
    assertNotNull(token.getJti());
    assertEquals("user123", token.getUserId());
    assertEquals("testuser", token.getUsername());
    assertEquals(List.of("ROLE_CUSTOMER"), token.getRoles());
    assertEquals("CUSTOMER", token.getUserType());
    assertEquals("ACCESS", token.getTokenType());
    assertNotNull(token.getIssuedAt());
    assertNotNull(token.getExpiresAt());
    assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
  }

  @Test
  void whenGenerateAccessTokenString_thenReturnsValidJwt() {
    // When
    String tokenString = jwtProvider.generateAccessTokenString(testUser);

    // Then
    assertNotNull(tokenString);
    assertEquals(3, tokenString.split("\\.").length); // JWT has 3 parts

    // Validate the token
    JwtToken jwtToken = jwtProvider.validateToken(tokenString);
    assertNotNull(jwtToken);
    assertEquals("user123", jwtToken.getUserId());
    assertEquals("testuser", jwtToken.getUsername());
    assertEquals("CUSTOMER", jwtToken.getUserType());
    assertEquals("ACCESS", jwtToken.getTokenType());
  }

  @Test
  void whenGenerateRefreshToken_thenReturnsValidJwt() {
    // When
    String refreshToken = jwtProvider.generateRefreshToken(testUser);

    // Then
    assertNotNull(refreshToken);
    assertEquals(3, refreshToken.split("\\.").length);

    // Validate the token
    JwtToken jwtToken = jwtProvider.validateToken(refreshToken);
    assertNotNull(jwtToken);
    assertEquals("user123", jwtToken.getUserId());
    assertEquals("REFRESH", jwtToken.getTokenType());
  }

  @Test
  void whenExtractJti_thenReturnsCorrectJti() {
    // Given
    String token = jwtProvider.generateAccessTokenString(testUser);

    // When
    String jti = jwtProvider.extractJti(token);

    // Then
    assertNotNull(jti);
    assertFalse(jti.isEmpty());
  }

  @Test
  void whenValidateToken_thenReturnsJwtToken() {
    // Given
    String token = jwtProvider.generateAccessTokenString(testUser);

    // When
    JwtToken jwtToken = jwtProvider.validateToken(token);

    // Then
    assertNotNull(jwtToken);
    assertEquals("user123", jwtToken.getUserId());
    assertEquals("testuser", jwtToken.getUsername());
    assertNotNull(jwtToken.getRoles());
    assertEquals("CUSTOMER", jwtToken.getUserType());
    assertEquals("ACCESS", jwtToken.getTokenType());
  }

  @Test
  void whenValidateInvalidToken_thenThrowsException() {
    // Given
    String invalidToken = "invalid.jwt.token";

    // When & Then
    assertThrows(Exception.class, () -> {
      jwtProvider.validateToken(invalidToken);
    });
  }

  @Test
  void whenTokenSignedWithDifferentKey_thenThrowsException() throws Exception {
    // Given - Create a different key pair
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair differentKeyPair = keyGen.generateKeyPair();

    JwtProviderImpl otherJwtProvider = new JwtProviderImpl(
        differentKeyPair.getPrivate(),
        differentKeyPair.getPublic(),
        86400000L,
        604800000L
    );

    String tokenFromOtherProvider = otherJwtProvider.generateAccessTokenString(testUser);

    // When & Then - This provider should not validate token from other provider
    assertThrows(Exception.class, () -> {
      jwtProvider.validateToken(tokenFromOtherProvider);
    });
  }

  @Test
  void whenGenerateMultipleTokens_thenEachHasUniqueJti() {
    // When
    String token1 = jwtProvider.generateAccessTokenString(testUser);
    String token2 = jwtProvider.generateAccessTokenString(testUser);
    String token3 = jwtProvider.generateAccessTokenString(testUser);

    // Then
    String jti1 = jwtProvider.extractJti(token1);
    String jti2 = jwtProvider.extractJti(token2);
    String jti3 = jwtProvider.extractJti(token3);

    assertNotEquals(jti1, jti2);
    assertNotEquals(jti1, jti3);
    assertNotEquals(jti2, jti3);
  }

  @Test
  void whenGetAccessTokenExpiration_thenReturnsSeconds() {
    // When
    long expirationSeconds = jwtProvider.getAccessTokenExpiration();

    // Then
    assertEquals(86400, expirationSeconds); // 24 hours in seconds
  }
}
