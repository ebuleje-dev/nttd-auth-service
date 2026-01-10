package com.nttd.banking.auth.application.usecase;

import com.nttd.banking.auth.domain.event.UserRegisteredEvent;
import com.nttd.banking.auth.domain.exception.UserAlreadyExistsException;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import com.nttd.banking.auth.domain.service.AuthDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the register use case.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCaseImpl implements RegisterUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserEventPublisher eventPublisher;
  private final AuthDomainService authDomainService;

  @Override
  public Mono<User> register(RegisterRequest request) {
    return validateUniqueUser(request)
        .then(Mono.defer(() -> {
          String passwordHash = passwordEncoder.encode(request.password());
          User user = authDomainService.createUser(
              request.username(),
              request.email(),
              passwordHash,
              request.documentType(),
              request.documentNumber(),
              request.phoneNumber(),
              request.userType()
          );

          return userRepository.save(user);
        }))
        .flatMap(savedUser -> {
          UserRegisteredEvent event = UserRegisteredEvent.builder()
              .userId(savedUser.getId())
              .username(savedUser.getUsername())
              .email(savedUser.getEmail())
              .documentType(savedUser.getDocumentType())
              .documentNumber(savedUser.getDocumentNumber())
              .phoneNumber(savedUser.getPhoneNumber())
              .userType(savedUser.getUserType().name())
              .registeredAt(savedUser.getCreatedAt())
              .build();

          return eventPublisher.publishUserRegistered(event)
              .thenReturn(savedUser);
        });
  }

  /**
   * Validates that username, email and document number are unique.
   */
  private Mono<Void> validateUniqueUser(RegisterRequest request) {
    return userRepository.existsByUsername(request.username())
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Username already exists"))
            : Mono.empty())
        .then(userRepository.existsByEmail(request.email()))
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Email already exists"))
            : Mono.empty())
        .then(userRepository.existsByDocumentNumber(request.documentNumber()))
        .flatMap(exists -> exists
            ? Mono.error(new UserAlreadyExistsException("Document number already exists"))
            : Mono.empty())
        .then();
  }
}
