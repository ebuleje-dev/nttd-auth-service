package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import com.nttd.banking.auth.domain.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper between User domain and UserEntity.
 */
@Component
public class UserEntityMapper {

  /**
   * Converts UserEntity to User domain model.
   */
  public User toDomain(UserEntity entity) {
    return User.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .email(entity.getEmail())
        .passwordHash(entity.getPasswordHash())
        .documentType(entity.getDocumentType())
        .documentNumber(entity.getDocumentNumber())
        .phoneNumber(entity.getPhoneNumber())
        .userType(entity.getUserType())
        .customerId(entity.getCustomerId())
        .roles(entity.getRoles())
        .active(entity.getActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .lastLogin(entity.getLastLogin())
        .build();
  }

  /**
   * Converts User domain model to UserEntity.
   */
  public UserEntity toEntity(User user) {
    return UserEntity.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .passwordHash(user.getPasswordHash())
        .documentType(user.getDocumentType())
        .documentNumber(user.getDocumentNumber())
        .phoneNumber(user.getPhoneNumber())
        .userType(user.getUserType())
        .customerId(user.getCustomerId())
        .roles(user.getRoles())
        .active(user.getActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .lastLogin(user.getLastLogin())
        .build();
  }
}
