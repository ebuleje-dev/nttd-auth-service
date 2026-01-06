package com.nttd.banking.auth.infrastructure.adapter.out.messaging;

/**
 * Kafka topics used by auth-service.
 */
public final class KafkaTopics {

  private KafkaTopics() {
    // Utility class
  }

  public static final String USER_REGISTERED = "auth.user.registered";
  public static final String USER_LOGIN = "auth.user.login";
  public static final String USER_LOGOUT = "auth.user.logout";
}
