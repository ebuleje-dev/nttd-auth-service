package com.nttd.banking.auth.domain.port.out;

import com.nttd.banking.auth.domain.model.JwtToken;
import com.nttd.banking.auth.domain.model.User;

/**
 * Port for JWT token operations.
 * This interface abstracts the JWT implementation details from the domain.
 */
public interface JwtProvider {

  /**
   * Generates an access token metadata for a user.
   *
   * @param user the user to generate the token for
   * @return JwtToken with metadata (jti, userId, username, roles, etc.)
   */
  JwtToken generateAccessToken(User user);

  /**
   * Generates an access token string (JWT) for a user.
   *
   * @param user the user to generate the token for
   * @return the signed JWT string
   */
  String generateAccessTokenString(User user);

  /**
   * Generates a refresh token for a user.
   *
   * @param user the user to generate the token for
   * @return the signed refresh token string
   */
  String generateRefreshToken(User user);

  /**
   * Validates a token and extracts its information.
   *
   * @param token the JWT string to validate
   * @return JwtToken with the extracted claims
   * @throws RuntimeException if token is invalid or expired
   */
  JwtToken validateToken(String token);

  /**
   * Extracts the JTI (JWT ID) from a token.
   *
   * @param token the JWT string
   * @return the JTI claim value
   */
  String extractJti(String token);

  /**
   * Gets the access token expiration time in seconds.
   *
   * @return expiration time in seconds
   */
  long getAccessTokenExpiration();
}
