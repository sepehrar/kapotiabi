package com.kapotiabi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuthResponse {
    private String token;
    private Instant expiresAt;
    private String role;
    private Integer userId;
    private String shopStatus;  // null for car_owner
}
