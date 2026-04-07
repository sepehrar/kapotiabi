package com.kapotiabi.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
  private String token;
  private Instant expiresAt;
  private String role;
  private Long userId;
  private String shopStatus; // null for car_owner
}
