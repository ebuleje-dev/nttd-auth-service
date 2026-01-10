package com.nttd.banking.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nttd.banking.auth.domain.event.UserRegisteredEvent;
import com.nttd.banking.auth.domain.exception.UserAlreadyExistsException;
import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.domain.port.out.PasswordEncoder;
import com.nttd.banking.auth.domain.port.out.UserEventPublisher;
import com.nttd.banking.auth.domain.port.out.UserRepository;
import com.nttd.banking.auth.domain.service.AuthDomainService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserEventPublisher eventPublisher;

  @Mock
  private AuthDomainService authDomainService;

  @InjectMocks
  private RegisterUseCaseImpl registerUseCase;

  private User testUser;
  private RegisterUseCase.RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id("user123")
        .username("testuser")
        .email("test@example.com")
        .passwordHash("$2a$12$hashedPassword")
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("+51987654321")
        .userType(UserType.CUSTOMER)
        .roles(List.of("ROLE_CUSTOMER"))
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    registerRequest = new RegisterUseCase.RegisterRequest(
        "testuser",
        "test@example.com",
        "Password123!",
        "DNI",
        "12345678",
        "+51987654321",
        "CUSTOMER"
    );
  }

  @Test
  void whenRegisterValidUser_thenReturnsUser() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
    when(authDomainService.createUser(
        anyString(), anyString(), anyString(), anyString(),
        anyString(), anyString(), anyString()
    )).thenReturn(testUser);
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
    when(eventPublisher.publishUserRegistered(any(UserRegisteredEvent.class)))
        .thenReturn(Mono.empty());

    // When & Then
    StepVerifier.create(registerUseCase.register(registerRequest))
        .assertNext(user -> {
          assertNotNull(user);
          assertEquals("testuser", user.getUsername());
          assertEquals("test@example.com", user.getEmail());
        })
        .verifyComplete();

    verify(eventPublisher).publishUserRegistered(any(UserRegisteredEvent.class));
  }

  @Test
  void whenRegisterDuplicateUsername_thenThrowsException() {
    // Given - All mocks needed due to eager evaluation of .then() arguments
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));

    // When & Then
    StepVerifier.create(registerUseCase.register(registerRequest))
        .expectError(UserAlreadyExistsException.class)
        .verify();
  }

  @Test
  void whenRegisterDuplicateEmail_thenThrowsException() {
    // Given - All mocks needed due to eager evaluation of .then() arguments
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));

    // When & Then
    StepVerifier.create(registerUseCase.register(registerRequest))
        .expectError(UserAlreadyExistsException.class)
        .verify();
  }

  @Test
  void whenRegisterDuplicateDocumentNumber_thenThrowsException() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(true));

    // When & Then
    StepVerifier.create(registerUseCase.register(registerRequest))
        .expectError(UserAlreadyExistsException.class)
        .verify();
  }
}
