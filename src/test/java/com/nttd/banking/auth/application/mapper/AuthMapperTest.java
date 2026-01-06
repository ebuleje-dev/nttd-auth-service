package com.nttd.banking.auth.application.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.model.enums.UserType;
import com.nttd.banking.auth.domain.port.in.LoginUseCase;
import com.nttd.banking.auth.model.dto.LoginResponse;
import com.nttd.banking.auth.model.dto.RegisterRequest;
import com.nttd.banking.auth.model.dto.RegisterResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthMapperTest {

  private AuthMapper authMapper;

  @BeforeEach
  void setUp() {
    authMapper = new AuthMapper();
  }

  @Test
  void whenToLoginResponse_thenMapsCorrectly() {
    // Given
    LoginUseCase.LoginResult loginResult = new LoginUseCase.LoginResult(
        "access.token.here",
        "refresh.token.here",
        86400L,
        "user123",
        "testuser",
        List.of("ROLE_CUSTOMER"),
        "CUSTOMER"
    );

    // When
    LoginResponse response = authMapper.toLoginResponse(loginResult);

    // Then
    assertNotNull(response);
    assertEquals("access.token.here", response.getAccessToken());
    assertEquals("refresh.token.here", response.getRefreshToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(86400L, response.getExpiresIn());
    assertEquals("user123", response.getUserId());
    assertEquals("testuser", response.getUsername());
    assertEquals(List.of("ROLE_CUSTOMER"), response.getRoles());
    assertEquals(LoginResponse.UserTypeEnum.CUSTOMER, response.getUserType());
  }

  @Test
  void whenToRegisterRequest_thenMapsCorrectly() {
    // Given
    RegisterRequest dto = new RegisterRequest();
    dto.setUsername("testuser");
    dto.setEmail("test@example.com");
    dto.setPassword("Password123!");
    dto.setDocumentType(RegisterRequest.DocumentTypeEnum.DNI);
    dto.setDocumentNumber("12345678");
    dto.setPhoneNumber("+51987654321");
    dto.setUserType(RegisterRequest.UserTypeEnum.CUSTOMER);

    // When
    var domainRequest = authMapper.toRegisterRequest(dto);

    // Then
    assertNotNull(domainRequest);
    assertEquals("testuser", domainRequest.username());
    assertEquals("test@example.com", domainRequest.email());
    assertEquals("Password123!", domainRequest.password());
    assertEquals("DNI", domainRequest.documentType());
    assertEquals("12345678", domainRequest.documentNumber());
    assertEquals("+51987654321", domainRequest.phoneNumber());
    assertEquals("CUSTOMER", domainRequest.userType());
  }

  @Test
  void whenToRegisterResponse_thenMapsCorrectly() {
    // Given
    User user = User.builder()
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

    // When
    RegisterResponse response = authMapper.toRegisterResponse(user);

    // Then
    assertNotNull(response);
    assertEquals("user123", response.getUserId());
    assertEquals("testuser", response.getUsername());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("User registered successfully", response.getMessage());
  }

  @Test
  void whenToRegisterRequestWithAllUserTypes_thenMapsCorrectly() {
    // Test all user types defined in RegisterRequest
    RegisterRequest.UserTypeEnum[] userTypes = {
        RegisterRequest.UserTypeEnum.CUSTOMER,
        RegisterRequest.UserTypeEnum.YANKI_USER,
        RegisterRequest.UserTypeEnum.BOOTCOIN_USER
    };

    for (RegisterRequest.UserTypeEnum userType : userTypes) {
      RegisterRequest dto = new RegisterRequest();
      dto.setUsername("user");
      dto.setEmail("email@test.com");
      dto.setPassword("pass");
      dto.setDocumentType(RegisterRequest.DocumentTypeEnum.DNI);
      dto.setDocumentNumber("123");
      dto.setPhoneNumber("+51987654321");
      dto.setUserType(userType);

      var domainRequest = authMapper.toRegisterRequest(dto);

      assertNotNull(domainRequest);
      assertEquals(userType.getValue(), domainRequest.userType());
    }
  }

  @Test
  void whenToRegisterRequestWithAllDocumentTypes_thenMapsCorrectly() {
    // Test all document types
    RegisterRequest.DocumentTypeEnum[] docTypes = {
        RegisterRequest.DocumentTypeEnum.DNI,
        RegisterRequest.DocumentTypeEnum.CEX,
        RegisterRequest.DocumentTypeEnum.PASAPORTE
    };

    for (RegisterRequest.DocumentTypeEnum docType : docTypes) {
      RegisterRequest dto = new RegisterRequest();
      dto.setUsername("user");
      dto.setEmail("email@test.com");
      dto.setPassword("pass");
      dto.setDocumentType(docType);
      dto.setDocumentNumber("123");
      dto.setPhoneNumber("+51987654321");
      dto.setUserType(RegisterRequest.UserTypeEnum.CUSTOMER);

      var domainRequest = authMapper.toRegisterRequest(dto);

      assertNotNull(domainRequest);
      assertEquals(docType.getValue(), domainRequest.documentType());
    }
  }
}
