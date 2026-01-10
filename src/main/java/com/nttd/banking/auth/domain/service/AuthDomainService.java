package com.nttd.banking.auth.domain.service;

import com.nttd.banking.auth.domain.exception.TooManyLoginAttemptsException;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Domain service containing pure business logic.
 * This service has no infrastructure dependencies - only domain concepts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthDomainService {

  private final TokenCacheRepository tokenCache;

  private static final int MAX_LOGIN_ATTEMPTS = 5;

  /**
   * Checks if user has exceeded max login attempts.
   *
   * @param username the username to check
   * @return Mono that completes if attempts are within limit
   * @throws TooManyLoginAttemptsException if max attempts exceeded
   */
  public Mono<Void> checkLoginAttempts(String username) {
    return tokenCache.getLoginAttempts(username)
        .flatMap(attempts -> {
          if (attempts >= MAX_LOGIN_ATTEMPTS) {
            log.warn("User {} exceeded max login attempts: {}", username, attempts);
            return Mono.error(new TooManyLoginAttemptsException(
                "Too many login attempts. Try again later."));
          }
          return Mono.empty();
        });
  }

  /**
   * Returns default roles based on user type.
   *
   * @param userType the user type
   * @return list of default roles
   */
  public List<String> getDefaultRoles(String userType) {
    return switch (userType) {
      case "ADMIN" -> List.of("ROLE_ADMIN");
      case "EMPLOYEE" -> List.of("ROLE_EMPLOYEE");
      case "CUSTOMER" -> List.of("ROLE_CUSTOMER");
      case "YANKI_USER" -> List.of("ROLE_YANKI_USER");
      case "BOOTCOIN_USER" -> List.of("ROLE_BOOTCOIN_USER");
      default -> List.of();
    };
  }

  /**
   * Creates a new user entity from registration data.
   *
   * @param username       the username
   * @param email          the email
   * @param passwordHash   the hashed password
   * @param documentType   the document type
   * @param documentNumber the document number
   * @param phoneNumber    the phone number
   * @param userType       the user type
   * @return the created User domain entity
   */
  public User createUser(String username, String email, String passwordHash,
      String documentType, String documentNumber, String phoneNumber, String userType) {
    return User.builder()
        .username(username)
        .email(email)
        .passwordHash(passwordHash)
        .documentType(documentType)
        .documentNumber(documentNumber)
        .phoneNumber(phoneNumber)
        .userType(UserType.valueOf(userType))
        .roles(getDefaultRoles(userType))
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Updates user's last login timestamp.
   *
   * @param user the user to update
   * @return the updated user
   */
  public User updateLastLogin(User user) {
    return User.builder()
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
        .updatedAt(LocalDateTime.now())
        .lastLogin(LocalDateTime.now())
        .build();
  }
}
