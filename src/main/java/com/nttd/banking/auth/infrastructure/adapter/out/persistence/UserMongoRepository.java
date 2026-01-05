package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for UserEntity.
 */
@Repository
public interface UserMongoRepository extends ReactiveMongoRepository<UserEntity, String> {
  Mono<UserEntity> findByUsername(String username);

  Mono<UserEntity> findByEmail(String email);

  Mono<UserEntity> findByDocumentNumber(String documentNumber);

  Mono<Boolean> existsByUsername(String username);

  Mono<Boolean> existsByEmail(String email);

  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
