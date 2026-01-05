package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB entity for Role.
 */
@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
  @Id
  private String id;

  @Indexed(unique = true)
  private String name;

  private String description;

  private List<String> permissions;

  private Boolean active;

  private LocalDateTime createdAt;
}
