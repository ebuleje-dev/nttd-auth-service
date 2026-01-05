package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import com.nttd.banking.auth.domain.model.enums.UserType;
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
 * MongoDB entity for User.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
  @Id
  private String id;

  @Indexed(unique = true)
  private String username;

  @Indexed(unique = true)
  private String email;

  private String passwordHash;

  private String documentType;

  @Indexed(unique = true)
  private String documentNumber;

  private String phoneNumber;

  private UserType userType;

  private String customerId;

  private List<String> roles;

  private Boolean active;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private LocalDateTime lastLogin;
}
