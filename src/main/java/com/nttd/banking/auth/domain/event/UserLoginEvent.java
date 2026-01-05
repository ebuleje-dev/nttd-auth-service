package com.nttd.banking.auth.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user logs in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginEvent {
  private String userId;
  private String username;
  private LocalDateTime loginAt;
  private String ipAddress;
  private String userAgent;
}
