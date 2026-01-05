package com.nttd.banking.auth.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nttd.banking.auth.domain.model.enums.UserType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for User entity.
 */
class UserTest {

  @Test
  void whenBuildUser_thenAllFieldsSet() {
    LocalDateTime now = LocalDateTime.now();
    User user = User.builder()
        .id("123")
        .username("jdoe")
        .email("john@example.com")
        .passwordHash("hashedPassword")
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("+51987654321")
        .userType(UserType.CUSTOMER)
        .customerId("cust-123")
        .roles(List.of("ROLE_CUSTOMER"))
        .active(true)
        .createdAt(now)
        .updatedAt(now)
        .lastLogin(now)
        .build();

    assertAll(
        () -> assertEquals("123", user.getId()),
        () -> assertEquals("jdoe", user.getUsername()),
        () -> assertEquals("john@example.com", user.getEmail()),
        () -> assertEquals(UserType.CUSTOMER, user.getUserType()),
        () -> assertTrue(user.getActive()),
        () -> assertEquals(1, user.getRoles().size())
    );
  }

  @Test
  void whenUserIsYankiUser_thenCustomerIdIsNull() {
    User user = User.builder()
        .userType(UserType.YANKI_USER)
        .customerId(null)
        .build();

    assertEquals(UserType.YANKI_USER, user.getUserType());
    assertNull(user.getCustomerId());
  }
}
