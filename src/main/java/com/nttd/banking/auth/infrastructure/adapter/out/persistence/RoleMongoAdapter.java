package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import com.nttd.banking.auth.domain.model.Role;
import com.nttd.banking.auth.domain.port.out.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB adapter implementing RoleRepository port.
 * Only loads when not in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
public class RoleMongoAdapter implements RoleRepository {

  private final RoleMongoRepository mongoRepository;
  private final RoleEntityMapper mapper;

  @Override
  public Mono<Role> findByName(String name) {
    return mongoRepository.findByName(name)
        .map(mapper::toDomain);
  }
}
