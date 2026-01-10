package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.model.JwkKey;
import com.nttd.banking.auth.domain.model.Jwks;
import com.nttd.banking.auth.domain.port.in.GetJwksUseCase;
import com.nttd.banking.auth.domain.port.out.JwtProvider;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of GetJwksUseCase.
 * Converts RSA public key to JWKS format for external JWT validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetJwksUseCaseImpl implements GetJwksUseCase {

  private static final String KEY_TYPE = "RSA";
  private static final String ALGORITHM = "RS256";
  private static final String KEY_USE = "sig";
  private static final String KEY_ID = "auth-service-key-1";

  private final JwtProvider jwtProvider;

  @Override
  public Mono<Jwks> getJwks() {
    return Mono.fromCallable(() -> {
      RSAPublicKey rsaPublicKey = (RSAPublicKey) jwtProvider.getPublicKey();

      JwkKey jwkKey = JwkKey.builder()
          .kty(KEY_TYPE)
          .alg(ALGORITHM)
          .use(KEY_USE)
          .kid(KEY_ID)
          .n(base64UrlEncode(rsaPublicKey.getModulus().toByteArray()))
          .e(base64UrlEncode(rsaPublicKey.getPublicExponent().toByteArray()))
          .build();

      log.debug("JWKS retrieved successfully");

      return Jwks.builder()
          .keys(List.of(jwkKey))
          .build();
    });
  }

  /**
   * Encodes bytes to Base64URL format without padding.
   */
  private String base64UrlEncode(byte[] bytes) {
    // Remove leading zero byte if present (BigInteger sign bit)
    if (bytes.length > 0 && bytes[0] == 0) {
      byte[] trimmed = new byte[bytes.length - 1];
      System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
      bytes = trimmed;
    }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
