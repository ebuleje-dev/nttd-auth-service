package com.nttd.banking.auth.infrastructure.adapter.out.persistence;

import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB adapter implementing UserRepository port.
 * Only loads when not in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
public class UserMongoAdapter implements UserRepository {

  private final UserMongoRepository mongoRepository;
  private final UserEntityMapper mapper;

  @Override
  public Mono<User> save(User user) {
    UserEntity entity = mapper.toEntity(user);
    return mongoRepository.save(entity)
        .map(mapper::toDomain);
  }

  @Override
  public Mono<User> findById(String id) {
    return mongoRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return mongoRepository.findByUsername(username)
        .map(mapper::toDomain);
  }

  @Override
  public Mono<User> findByEmail(String email) {
    return mongoRepository.findByEmail(email)
        .map(mapper::toDomain);
  }

  @Override
  public Mono<User> findByDocumentNumber(String documentNumber) {
    return mongoRepository.findByDocumentNumber(documentNumber)
        .map(mapper::toDomain);
  }

  @Override
  public Mono<Boolean> existsByUsername(String username) {
    return mongoRepository.existsByUsername(username);
  }

  @Override
  public Mono<Boolean> existsByEmail(String email) {
    return mongoRepository.existsByEmail(email);
  }

  @Override
  public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
    return mongoRepository.existsByDocumentNumber(documentNumber);
  }
}
