package com.nttd.banking.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON Web Key (JWK) domain model.
 * Represents a single RSA public key in JWK format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwkKey {
  private String kty;  // Key Type (RSA)
  private String alg;  // Algorithm (RS256)
  private String use;  // Usage (sig = signature)
  private String kid;  // Key ID
  private String n;    // RSA Modulus (Base64URL)
  private String e;    // RSA Exponent (Base64URL)
}
