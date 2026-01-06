package com.nttd.banking.auth.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for circuit breaker pattern.
 * Provides fault tolerance for external service calls.
 */
@Configuration
@Slf4j
public class Resilience4jConfig {

  /**
   * Circuit breaker for external validations and service calls.
   *
   * <p>Configuration:
   * <ul>
   *   <li>Sliding window size: 10 calls</li>
   *   <li>Failure rate threshold: 50%</li>
   *   <li>Wait duration in open state: 10 seconds</li>
   *   <li>Permitted calls in half-open state: 3</li>
   *   <li>Slow call threshold: 2 seconds</li>
   *   <li>Slow call rate threshold: 50%</li>
   * </ul>
   *
   * @return configured CircuitBreaker instance
   */
  @Bean
  public CircuitBreaker validationCircuitBreaker() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .slidingWindowSize(10)
        .failureRateThreshold(50.0f)
        .waitDurationInOpenState(Duration.ofSeconds(10))
        .permittedNumberOfCallsInHalfOpenState(3)
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .slowCallRateThreshold(50.0f)
        .build();

    CircuitBreaker circuitBreaker = CircuitBreaker.of("validation", config);

    // Register event listeners for monitoring
    circuitBreaker.getEventPublisher()
        .onStateTransition(event ->
            log.warn("Circuit Breaker state transition: {}", event))
        .onFailureRateExceeded(event ->
            log.error("Circuit Breaker failure rate exceeded: {}%",
                event.getFailureRate()))
        .onCallNotPermitted(event ->
            log.warn("Circuit Breaker call not permitted"))
        .onError(event ->
            log.error("Circuit Breaker error occurred: {}",
                event.getThrowable().getMessage()));

    return circuitBreaker;
  }
}
