package com.kapotiabi.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

  private String secret;
  private long expirationMs;

  @PostConstruct
  void ensureSecret() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET is required (>= 32 bytes)");
    }
  }
}
