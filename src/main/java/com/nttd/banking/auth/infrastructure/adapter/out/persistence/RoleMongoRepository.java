package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for RoleEntity.
 */
@Repository
public interface RoleMongoRepository extends ReactiveMongoRepository<RoleEntity, String> {
  Mono<RoleEntity> findByName(String name);
}
