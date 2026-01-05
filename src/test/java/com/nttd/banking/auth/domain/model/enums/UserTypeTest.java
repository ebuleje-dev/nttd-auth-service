package com.nttd.banking.auth.domain.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserType enum.
 */
class UserTypeTest {

  @Test
  void whenUserTypeValues_thenHasFiveTypes() {
    UserType[] types = UserType.values();
    assertEquals(5, types.length);
  }

  @Test
  void whenValueOf_thenReturnsCorrectEnum() {
    assertEquals(UserType.ADMIN, UserType.valueOf("ADMIN"));
    assertEquals(UserType.EMPLOYEE, UserType.valueOf("EMPLOYEE"));
    assertEquals(UserType.CUSTOMER, UserType.valueOf("CUSTOMER"));
    assertEquals(UserType.YANKI_USER, UserType.valueOf("YANKI_USER"));
    assertEquals(UserType.BOOTCOIN_USER, UserType.valueOf("BOOTCOIN_USER"));
  }
}
