package com.nttd.banking.auth.infrastructure.adapter.out.security;

import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT provider implementation using JJWT library.
 * Only loads when not in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class JwtProviderImpl implements JwtProvider {

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  @Override
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

  @Override
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

  @Override
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

  @Override
  public JwtToken validateToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");

    return JwtToken.builder()
        .jti(claims.getId())
        .userId(claims.getSubject())
        .username(claims.get("username", String.class))
        .roles(roles)
        .userType(claims.get("userType", String.class))
        .issuedAt(toLocalDateTime(claims.getIssuedAt()))
        .expiresAt(toLocalDateTime(claims.getExpiration()))
        .tokenType(claims.get("tokenType", String.class))
        .build();
  }

  @Override
  public String extractJti(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.getId();
  }

  @Override
  public long getAccessTokenExpiration() {
    return accessTokenExpiration / 1000; // Convert to seconds
  }

  /**
   * Converts Date to LocalDateTime.
   */
  private LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
