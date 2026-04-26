package com.kapotiabi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kapotiabi.enums.RepairShopStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
  private String token;
  private Instant expiresAt;
  private String role;
  private Long userId;
  private RepairShopStatus shopStatus;
}
