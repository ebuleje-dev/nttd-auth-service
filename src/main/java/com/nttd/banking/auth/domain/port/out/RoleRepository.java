package com.nttd.banking.auth.domain.port.out;

import com.nttd.banking.auth.domain.model.Role;
import reactor.core.publisher.Mono;

/**
 * Role repository port.
 */
public interface RoleRepository {
  Mono<Role> findByName(String name);
}
