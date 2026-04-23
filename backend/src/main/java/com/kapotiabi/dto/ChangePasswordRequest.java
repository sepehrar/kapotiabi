package com.kapotiabi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
        String newPassword) {}
