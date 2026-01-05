package com.nttd.banking.auth.domain.model;

import com.nttd.banking.auth.domain.model.enums.UserType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User domain entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private String id;
  private String username;
  private String email;
  private String passwordHash;
  private String documentType;
  private String documentNumber;
  private String phoneNumber;
  private UserType userType;
  private String customerId; // Null for YANKI_USER and BOOTCOIN_USER
  private List<String> roles;
  private Boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime lastLogin;
}
