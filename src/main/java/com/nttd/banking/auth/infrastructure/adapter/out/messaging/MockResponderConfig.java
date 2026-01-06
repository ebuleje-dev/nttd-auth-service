package com.nttd.banking.auth.infrastructure.adapter.out.messaging;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

/**
 * Mock responder for consuming events in local development.
 * Only active when kafka.mock.enabled=true.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockResponderConfig {

  private final ReceiverOptions<String, String> receiverOptions;

  /**
   * Starts mock Kafka responders after bean initialization.
   */
  @PostConstruct
  public void startMockResponders() {
    log.info("Starting Kafka mock responders for local development");

    // Subscribe to user-registered events
    ReceiverOptions<String, String> userRegisteredOptions = receiverOptions
        .subscription(Collections.singleton(KafkaTopics.USER_REGISTERED));

    KafkaReceiver.create(userRegisteredOptions)
        .receive()
        .doOnNext(record -> {
          log.info("Mock responder received UserRegisteredEvent: key={}, value={}",
              record.key(), record.value());
          record.receiverOffset().acknowledge();
        })
        .doOnError(error -> log.error("Error in UserRegistered mock responder", error))
        .subscribe();

    // Subscribe to user-login events
    ReceiverOptions<String, String> userLoginOptions = receiverOptions
        .subscription(Collections.singleton(KafkaTopics.USER_LOGIN));

    KafkaReceiver.create(userLoginOptions)
        .receive()
        .doOnNext(record -> {
          log.info("Mock responder received UserLoginEvent: key={}, value={}",
              record.key(), record.value());
          record.receiverOffset().acknowledge();
        })
        .doOnError(error -> log.error("Error in UserLogin mock responder", error))
        .subscribe();

    log.info("Kafka mock responders started successfully");
  }
}
