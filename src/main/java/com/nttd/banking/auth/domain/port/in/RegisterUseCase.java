package com.nttd.banking.auth.domain.port.in;

import com.nttd.banking.auth.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Use case for user registration.
 */
public interface RegisterUseCase {
  /**
   * Registers a new user.
   *
   * @param request Registration data
   * @return Mono with created user
   */
  Mono<User> register(RegisterRequest request);

  /**
   * Registration request data.
   */
  record RegisterRequest(
      String username,
      String email,
      String password,
      String documentType,
      String documentNumber,
      String phoneNumber,
      String userType
  ) {}
}
