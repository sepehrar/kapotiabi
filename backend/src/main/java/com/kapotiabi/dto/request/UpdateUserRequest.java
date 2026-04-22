package com.kapotiabi.dto.request;

import com.kapotiabi.enums.PartsQuality;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    String phone,
    @Size(max = 100) String preferredCity,
    @Min(1) @Max(250) Integer searchRadiusKm,
    PartsQuality partsQualityPreference) {}
