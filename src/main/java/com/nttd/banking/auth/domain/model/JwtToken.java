package com.nttd.banking.auth.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT Token domain model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
  private String jti; // Token ID
  private String userId;
  private String username;
  private List<String> roles;
  private String userType;
  private LocalDateTime issuedAt;
  private LocalDateTime expiresAt;
  private String tokenType; // ACCESS or REFRESH
}
