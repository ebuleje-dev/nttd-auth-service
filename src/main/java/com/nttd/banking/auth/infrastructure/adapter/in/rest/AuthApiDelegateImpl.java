package com.nttd.banking.auth.infrastructure.adapter.in.rest;

import com.nttd.banking.auth.api.ApiApiDelegate;
import com.nttd.banking.auth.application.mapper.AuthMapper;
import com.nttd.banking.auth.domain.port.in.LoginUseCase;
import com.nttd.banking.auth.domain.port.in.LogoutUseCase;
import com.nttd.banking.auth.domain.port.in.RegisterUseCase;
import com.nttd.banking.auth.model.dto.LoginRequest;
import com.nttd.banking.auth.model.dto.LoginResponse;
import com.nttd.banking.auth.model.dto.LogoutResponse;
import com.nttd.banking.auth.model.dto.RefreshTokenRequest;
import com.nttd.banking.auth.model.dto.RefreshTokenResponse;
import com.nttd.banking.auth.model.dto.RegisterRequest;
import com.nttd.banking.auth.model.dto.RegisterResponse;
import com.nttd.banking.auth.model.dto.ValidateTokenRequest;
import com.nttd.banking.auth.model.dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Implementation of OpenAPI-generated ApiApiDelegate.
 * Only loads when not in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class AuthApiDelegateImpl implements ApiApiDelegate {

  private final LoginUseCase loginUseCase;
  private final RegisterUseCase registerUseCase;
  private final LogoutUseCase logoutUseCase;
  private final AuthMapper mapper;

  @Override
  public Mono<ResponseEntity<LoginResponse>> login(
      Mono<LoginRequest> loginRequest,
      ServerWebExchange exchange) {

    return loginRequest
        .flatMap(req -> loginUseCase.login(req.getUsername(), req.getPassword()))
        .map(mapper::toLoginResponse)
        .map(ResponseEntity::ok)
        .doOnSuccess(res -> log.info("User logged in successfully"));
  }

  @Override
  public Mono<ResponseEntity<RegisterResponse>> register(
      Mono<RegisterRequest> registerRequest,
      ServerWebExchange exchange) {

    return registerRequest
        .map(mapper::toRegisterRequest)
        .flatMap(registerUseCase::register)
        .map(mapper::toRegisterResponse)
        .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res))
        .doOnSuccess(res -> log.info("User registered successfully"));
  }

  @Override
  public Mono<ResponseEntity<LogoutResponse>> logout(ServerWebExchange exchange) {
    // Extract token from Authorization header
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    String token = authHeader.substring(7);

    return logoutUseCase.logout(token)
        .then(Mono.fromCallable(() -> {
          LogoutResponse response = new LogoutResponse();
          response.setMessage("Logout successful");
          return ResponseEntity.ok(response);
        }))
        .doOnSuccess(res -> log.info("User logged out successfully"));
  }

  @Override
  public Mono<ResponseEntity<ValidateTokenResponse>> validateToken(
      Mono<ValidateTokenRequest> validateTokenRequest,
      ServerWebExchange exchange) {
    // TODO: Implement in next iteration
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
  }

  @Override
  public Mono<ResponseEntity<RefreshTokenResponse>> refreshToken(
      Mono<RefreshTokenRequest> refreshTokenRequest,
      ServerWebExchange exchange) {
    // TODO: Implement in next iteration
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
  }
}
