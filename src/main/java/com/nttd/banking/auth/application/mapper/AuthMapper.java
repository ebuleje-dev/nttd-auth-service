package com.nttd.banking.auth.application.mapper;

import com.nttd.banking.auth.domain.model.User;
import com.nttd.banking.auth.domain.port.in.LoginUseCase;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.model.dto.LoginResponse;
import com.nttd.banking.auth.model.dto.RegisterRequest;
import com.nttd.banking.auth.model.dto.RegisterResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper between DTOs and domain models.
 */
@Component
public class AuthMapper {

  /**
   * Converts LoginResult to LoginResponse DTO.
   */
  public LoginResponse toLoginResponse(LoginUseCase.LoginResult result) {
    LoginResponse response = new LoginResponse();
    response.setAccessToken(result.accessToken());
    response.setRefreshToken(result.refreshToken());
    response.setTokenType("Bearer");
    response.setExpiresIn(result.expiresIn());
    response.setUserId(result.userId());
    response.setUsername(result.username());
    response.setRoles(result.roles());
    response.setUserType(LoginResponse.UserTypeEnum.fromValue(result.userType()));
    return response;
  }

  /**
   * Converts RegisterRequest DTO to domain RegisterRequest.
   */
  public RegisterUseCase.RegisterRequest toRegisterRequest(RegisterRequest dto) {
    return new RegisterUseCase.RegisterRequest(
        dto.getUsername(),
        dto.getEmail(),
        dto.getPassword(),
        dto.getDocumentType().getValue(),
        dto.getDocumentNumber(),
        dto.getPhoneNumber(),
        dto.getUserType().getValue()
    );
  }

  /**
   * Converts User domain to RegisterResponse DTO.
   */
  public RegisterResponse toRegisterResponse(User user) {
    RegisterResponse response = new RegisterResponse();
    response.setUserId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setMessage("User registered successfully");
    return response;
  }
}
