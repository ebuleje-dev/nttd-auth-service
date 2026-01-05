package com.nttd.banking.auth.domain.port.out;

/**
 * Password encoder port.
 */
public interface PasswordEncoder {
  String encode(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}
