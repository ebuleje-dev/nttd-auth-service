package com.nttd.banking.auth.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for reactive operations.
 * Only loads when not in test profile.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class RedisConfig {

  /**
   * Reactive Redis Template for String operations.
   */
  @Bean
  @Primary
  public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {

    RedisSerializationContext<String, String> serializationContext =
        RedisSerializationContext.<String, String>newSerializationContext()
            .key(StringRedisSerializer.UTF_8)
            .value(StringRedisSerializer.UTF_8)
            .hashKey(StringRedisSerializer.UTF_8)
            .hashValue(StringRedisSerializer.UTF_8)
            .build();

    return new ReactiveRedisTemplate<>(factory, serializationContext);
  }
}
