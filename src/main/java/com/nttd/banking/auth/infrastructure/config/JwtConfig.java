package com.nttd.banking.auth.infrastructure.config;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * JWT configuration with RSA keys.
 * Only loads when not in test profile.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
@Slf4j
public class JwtConfig {

  @Value("${jwt.private-key}")
  private Resource privateKeyResource;

  @Value("${jwt.public-key}")
  private Resource publicKeyResource;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  /**
   * Loads RSA private key from PEM file.
   */
  @Bean
  public PrivateKey privateKey() throws Exception {
    String keyContent = new String(privateKeyResource.getInputStream().readAllBytes())
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(keyContent);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory factory = KeyFactory.getInstance("RSA");

    log.info("RSA private key loaded successfully");
    return factory.generatePrivate(spec);
  }

  /**
   * Loads RSA public key from PEM file.
   */
  @Bean
  public PublicKey publicKey() throws Exception {
    String keyContent = new String(publicKeyResource.getInputStream().readAllBytes())
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(keyContent);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    KeyFactory factory = KeyFactory.getInstance("RSA");

    log.info("RSA public key loaded successfully");
    return factory.generatePublic(spec);
  }

  @Bean
  public long accessTokenExpiration() {
    return accessTokenExpiration;
  }

  @Bean
  public long refreshTokenExpiration() {
    return refreshTokenExpiration;
  }
}
