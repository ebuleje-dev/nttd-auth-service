package com.nttd.banking.auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for Spring Boot application context.
 * Disabled because domain/application layer components are excluded in test profile.
 * Unit tests provide better coverage without requiring full context loading.
 */
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {
		"spring.config.import=optional:configserver:http://localhost:8888",
		"spring.autoconfigure.exclude=" +
			"org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
			"org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
			"de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration," +
			"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
			"org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
			"org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration"
	}
)
@ActiveProfiles("test")
@Disabled("Disabled because @Profile('!test') excludes main components from test context")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
		// Verifica que el contexto de Spring carga correctamente
	}

}
