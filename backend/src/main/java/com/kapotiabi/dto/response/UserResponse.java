package com.kapotiabi.dto.response;

import com.kapotiabi.entity.UserEntity;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    String role,
    String firstName,
    String lastName,
    String phone,
    String preferredCity,
    Integer searchRadiusKm,
    String partsQualityPreference,
    LocalDateTime createdAt) {
  public static UserResponse from(UserEntity u) {
    return new UserResponse(
        u.getId(),
        u.getEmail(),
        u.getRole().getDbValue(),
        u.getFirstName(),
        u.getLastName(),
        u.getPhone(),
        u.getPreferredCity(),
        u.getSearchRadiusKm(),
        u.getPartsQualityPreference(),
        u.getCreatedAt());
  }
}
