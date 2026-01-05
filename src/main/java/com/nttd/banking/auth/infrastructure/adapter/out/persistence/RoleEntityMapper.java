package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import com.nttd.banking.auth.domain.model.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper between Role domain and RoleEntity.
 */
@Component
public class RoleEntityMapper {

  /**
   * Converts RoleEntity to Role domain model.
   */
  public Role toDomain(RoleEntity entity) {
    return Role.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .permissions(entity.getPermissions())
        .active(entity.getActive())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  /**
   * Converts Role domain model to RoleEntity.
   */
  public RoleEntity toEntity(Role role) {
    return RoleEntity.builder()
        .id(role.getId())
        .name(role.getName())
        .description(role.getDescription())
        .permissions(role.getPermissions())
        .active(role.getActive())
        .createdAt(role.getCreatedAt())
        .build();
  }
}
