package com.nttd.banking.auth.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON Web Key Set (JWKS) domain model.
 * Contains a list of public keys used for JWT verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jwks {
  private List<JwkKey> keys;
}
