package com.nttd.banking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Auth Service.
 * Provides authentication and authorization for NTT Data Banking Platform.
 */
@SpringBootApplication
public class AuthServiceApplication {

  /**
   * Main entry point for the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }

}
