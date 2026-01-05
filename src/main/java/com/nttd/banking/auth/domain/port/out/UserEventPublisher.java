package com.nttd.banking.auth.domain.port.out;

import com.nttd.banking.auth.domain.event.UserLoginEvent;
import com.nttd.banking.auth.domain.event.UserRegisteredEvent;
import reactor.core.publisher.Mono;

/**
 * User event publisher port (Kafka).
 */
public interface UserEventPublisher {
  Mono<Void> publishUserRegistered(UserRegisteredEvent event);

  Mono<Void> publishUserLogin(UserLoginEvent event);
}
