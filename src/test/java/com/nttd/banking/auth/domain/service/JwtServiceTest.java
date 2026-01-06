package com.nttd.banking.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import io.jsonwebtoken.Claims;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;
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

    jwtService = new JwtService(
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
    JwtToken token = jwtService.generateAccessToken(testUser);

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
    String tokenString = jwtService.generateAccessTokenString(testUser);

    // Then
    assertNotNull(tokenString);
    assertTrue(tokenString.split("\\.").length == 3); // JWT has 3 parts

    // Validate the token
    Claims claims = jwtService.validateAndGetClaims(tokenString);
    assertNotNull(claims);
    assertEquals("user123", claims.getSubject());
    assertEquals("testuser", claims.get("username", String.class));
    assertEquals("CUSTOMER", claims.get("userType", String.class));
    assertEquals("ACCESS", claims.get("tokenType", String.class));
  }

  @Test
  void whenGenerateRefreshToken_thenReturnsValidJwt() {
    // When
    String refreshToken = jwtService.generateRefreshToken(testUser);

    // Then
    assertNotNull(refreshToken);
    assertTrue(refreshToken.split("\\.").length == 3);

    // Validate the token
    Claims claims = jwtService.validateAndGetClaims(refreshToken);
    assertNotNull(claims);
    assertEquals("user123", claims.getSubject());
    assertEquals("REFRESH", claims.get("tokenType", String.class));
  }

  @Test
  void whenExtractJti_thenReturnsCorrectJti() {
    // Given
    String token = jwtService.generateAccessTokenString(testUser);

    // When
    String jti = jwtService.extractJti(token);

    // Then
    assertNotNull(jti);
    assertFalse(jti.isEmpty());
  }

  @Test
  void whenValidateAndGetClaims_thenReturnsClaims() {
    // Given
    String token = jwtService.generateAccessTokenString(testUser);

    // When
    Claims claims = jwtService.validateAndGetClaims(token);

    // Then
    assertNotNull(claims);
    assertEquals("user123", claims.getSubject());
    assertEquals("testuser", claims.get("username", String.class));
    assertEquals("test@example.com", claims.get("email", String.class));
    assertNotNull(claims.get("roles"));
    assertEquals("CUSTOMER", claims.get("userType", String.class));
    assertEquals("ACCESS", claims.get("tokenType", String.class));
  }

  @Test
  void whenValidateInvalidToken_thenThrowsException() {
    // Given
    String invalidToken = "invalid.jwt.token";

    // When & Then
    assertThrows(Exception.class, () -> {
      jwtService.validateAndGetClaims(invalidToken);
    });
  }

  @Test
  void whenTokenSignedWithDifferentKey_thenThrowsException() throws Exception {
    // Given - Create a different key pair
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair differentKeyPair = keyGen.generateKeyPair();

    JwtService otherJwtService = new JwtService(
        differentKeyPair.getPrivate(),
        differentKeyPair.getPublic(),
        86400000L,
        604800000L
    );

    String tokenFromOtherService = otherJwtService.generateAccessTokenString(testUser);

    // When & Then - This service should not validate token from other service
    assertThrows(Exception.class, () -> {
      jwtService.validateAndGetClaims(tokenFromOtherService);
    });
  }

  @Test
  void whenGenerateMultipleTokens_thenEachHasUniqueJti() {
    // When
    String token1 = jwtService.generateAccessTokenString(testUser);
    String token2 = jwtService.generateAccessTokenString(testUser);
    String token3 = jwtService.generateAccessTokenString(testUser);

    // Then
    String jti1 = jwtService.extractJti(token1);
    String jti2 = jwtService.extractJti(token2);
    String jti3 = jwtService.extractJti(token3);

    assertNotEquals(jti1, jti2);
    assertNotEquals(jti1, jti3);
    assertNotEquals(jti2, jti3);
  }
}
