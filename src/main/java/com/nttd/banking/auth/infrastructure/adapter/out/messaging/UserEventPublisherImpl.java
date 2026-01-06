package com.nttd.banking.auth.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nttd.banking.auth.domain.event.UserLoginEvent;
import com.nttd.banking.auth.domain.event.UserRegisteredEvent;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

/**
 * Kafka implementation of UserEventPublisher.
 * Publishes user events to Kafka topics.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisherImpl implements UserEventPublisher {

  private final SenderOptions<String, String> senderOptions;
  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @Override
  public Mono<Void> publishUserRegistered(UserRegisteredEvent event) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
        .flatMap(json -> {
          KafkaSender<String, String> sender = KafkaSender.create(senderOptions);
          ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
              KafkaTopics.USER_REGISTERED,
              event.getUserId(),
              json
          );

          SenderRecord<String, String, String> record = SenderRecord.create(
              producerRecord,
              event.getUserId()
          );

          return sender.send(Mono.just(record))
              .doOnNext(result -> log.info(
                  "Published UserRegisteredEvent for user: {} to offset: {}",
                  event.getUserId(), result.recordMetadata().offset()))
              .doOnError(error -> log.error(
                  "Error publishing UserRegisteredEvent", error))
              .doFinally(signalType -> sender.close())
              .then();
        })
        .onErrorResume(JsonProcessingException.class, e -> {
          log.error("Error serializing UserRegisteredEvent", e);
          return Mono.empty();
        });
  }

  @Override
  public Mono<Void> publishUserLogin(UserLoginEvent event) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
        .flatMap(json -> {
          KafkaSender<String, String> sender = KafkaSender.create(senderOptions);
          ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
              KafkaTopics.USER_LOGIN,
              event.getUserId(),
              json
          );

          SenderRecord<String, String, String> record = SenderRecord.create(
              producerRecord,
              event.getUserId()
          );

          return sender.send(Mono.just(record))
              .doOnNext(result -> log.info(
                  "Published UserLoginEvent for user: {} at offset: {}",
                  event.getUserId(), result.recordMetadata().offset()))
              .doOnError(error -> log.error(
                  "Error publishing UserLoginEvent", error))
              .doFinally(signalType -> sender.close())
              .then();
        })
        .onErrorResume(JsonProcessingException.class, e -> {
          log.error("Error serializing UserLoginEvent", e);
          return Mono.empty();
        });
  }
}
