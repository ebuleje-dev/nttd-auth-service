package com.nttd.banking.auth.domain.service;

import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * JWT service for token generation and validation.
 * Only loads when not in test profile.
 */
@Service
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  /**
   * Generates an access token for a user and returns metadata.
   */
  public JwtToken generateAccessToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
    String jti = UUID.randomUUID().toString();

    log.debug("Generated access token for user: {}", user.getUsername());

    return JwtToken.builder()
        .jti(jti)
        .userId(user.getId())
        .username(user.getUsername())
        .roles(user.getRoles())
        .userType(user.getUserType().name())
        .issuedAt(toLocalDateTime(now))
        .expiresAt(toLocalDateTime(expiryDate))
        .tokenType("ACCESS")
        .build();
  }

  /**
   * Generates an access token string (JWT) for a user.
   */
  public String generateAccessTokenString(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getId())
        .claim("username", user.getUsername())
        .claim("email", user.getEmail())
        .claim("roles", user.getRoles())
        .claim("userType", user.getUserType().name())
        .claim("tokenType", "ACCESS")
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
  }

  /**
   * Generates a refresh token for a user.
   */
  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getId())
        .claim("tokenType", "REFRESH")
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
  }

  /**
   * Validates a token and extracts claims.
   */
  public Claims validateAndGetClaims(String token) {
    return Jwts.parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Extracts JTI from token.
   */
  public String extractJti(String token) {
    return validateAndGetClaims(token).getId();
  }

  /**
   * Converts Date to LocalDateTime.
   */
  private LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
