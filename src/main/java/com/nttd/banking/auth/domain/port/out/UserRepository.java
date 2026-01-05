package com.nttd.banking.auth.domain.port.out;

import com.nttd.banking.auth.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * User repository port.
 */
public interface UserRepository {
  Mono<User> save(User user);

  Mono<User> findById(String id);

  Mono<User> findByUsername(String username);

  Mono<User> findByEmail(String email);

  Mono<User> findByDocumentNumber(String documentNumber);

  Mono<Boolean> existsByUsername(String username);

  Mono<Boolean> existsByEmail(String email);

  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
