package com.nttd.banking.auth.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user is registered.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
  private String userId;
  private String username;
  private String email;
  private String documentType;
  private String documentNumber;
  private String phoneNumber;
  private String userType;
  private LocalDateTime registeredAt;
}
