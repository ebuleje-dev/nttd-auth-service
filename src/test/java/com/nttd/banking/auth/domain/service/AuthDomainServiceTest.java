package com.nttd.banking.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.exception.TooManyLoginAttemptsException;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.out.TokenCacheRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthDomainServiceTest {

  @Mock
  private TokenCacheRepository tokenCache;

  @InjectMocks
  private AuthDomainService authDomainService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id("user123")
        .username("testuser")
        .email("test@example.com")
        .passwordHash("$2a$12$hashedPassword")
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("+51987654321")
        .userType(UserType.CUSTOMER)
        .roles(List.of("ROLE_CUSTOMER"))
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  @Test
  void whenCheckLoginAttemptsWithinLimit_thenCompletes() {
    // Given
    when(tokenCache.getLoginAttempts(anyString())).thenReturn(Mono.just(2L));

    // When & Then
    StepVerifier.create(authDomainService.checkLoginAttempts("testuser"))
        .verifyComplete();
  }

  @Test
  void whenCheckLoginAttemptsExceedsLimit_thenThrowsException() {
    // Given
    when(tokenCache.getLoginAttempts(anyString())).thenReturn(Mono.just(5L));

    // When & Then
    StepVerifier.create(authDomainService.checkLoginAttempts("testuser"))
        .expectError(TooManyLoginAttemptsException.class)
        .verify();
  }

  @Test
  void whenGetDefaultRolesForAdmin_thenReturnsAdminRole() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("ADMIN");

    // Then
    assertEquals(List.of("ROLE_ADMIN"), roles);
  }

  @Test
  void whenGetDefaultRolesForEmployee_thenReturnsEmployeeRole() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("EMPLOYEE");

    // Then
    assertEquals(List.of("ROLE_EMPLOYEE"), roles);
  }

  @Test
  void whenGetDefaultRolesForCustomer_thenReturnsCustomerRole() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("CUSTOMER");

    // Then
    assertEquals(List.of("ROLE_CUSTOMER"), roles);
  }

  @Test
  void whenGetDefaultRolesForYankiUser_thenReturnsYankiRole() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("YANKI_USER");

    // Then
    assertEquals(List.of("ROLE_YANKI_USER"), roles);
  }

  @Test
  void whenGetDefaultRolesForBootcoinUser_thenReturnsBootcoinRole() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("BOOTCOIN_USER");

    // Then
    assertEquals(List.of("ROLE_BOOTCOIN_USER"), roles);
  }

  @Test
  void whenGetDefaultRolesForUnknownType_thenReturnsEmptyList() {
    // When
    List<String> roles = authDomainService.getDefaultRoles("UNKNOWN");

    // Then
    assertTrue(roles.isEmpty());
  }

  @Test
  void whenCreateUser_thenReturnsUserWithCorrectData() {
    // When
    User user = authDomainService.createUser(
        "testuser",
        "test@example.com",
        "$2a$12$hashedPassword",
        "DNI",
        "12345678",
        "+51987654321",
        "CUSTOMER"
    );

    // Then
    assertNotNull(user);
    assertEquals("testuser", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("$2a$12$hashedPassword", user.getPasswordHash());
    assertEquals("DNI", user.getDocumentType());
    assertEquals("12345678", user.getDocumentNumber());
    assertEquals("+51987654321", user.getPhoneNumber());
    assertEquals(UserType.CUSTOMER, user.getUserType());
    assertEquals(List.of("ROLE_CUSTOMER"), user.getRoles());
    assertTrue(user.getActive());
    assertNotNull(user.getCreatedAt());
    assertNotNull(user.getUpdatedAt());
  }

  @Test
  void whenUpdateLastLogin_thenReturnsUserWithUpdatedTimestamps() {
    // When
    User updatedUser = authDomainService.updateLastLogin(testUser);

    // Then
    assertNotNull(updatedUser);
    assertEquals(testUser.getId(), updatedUser.getId());
    assertEquals(testUser.getUsername(), updatedUser.getUsername());
    assertNotNull(updatedUser.getLastLogin());
    assertNotNull(updatedUser.getUpdatedAt());
  }
}
