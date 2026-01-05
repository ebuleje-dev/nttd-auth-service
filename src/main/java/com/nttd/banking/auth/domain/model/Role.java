package com.nttd.banking.auth.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role domain entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
  private String id;
  private String name; // ROLE_ADMIN, ROLE_EMPLOYEE, etc.
  private String description;
  private List<String> permissions;
  private Boolean active;
  private LocalDateTime createdAt;
}
