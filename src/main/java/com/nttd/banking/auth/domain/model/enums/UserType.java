package com.nttd.banking.auth.domain.model.enums;

/**
 * User types in the banking platform.
 */
public enum UserType {
  /** Administrator of the system. */
  ADMIN,

  /** Bank employee. */
  EMPLOYEE,

  /** Bank customer (has customerId). */
  CUSTOMER,

  /** Yanki wallet user (no customerId). */
  YANKI_USER,

  /** BootCoin user (no customerId). */
  BOOTCOIN_USER
}
