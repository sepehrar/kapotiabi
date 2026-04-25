package com.kapotiabi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CarRequest(
    @NotBlank String plateNumber, String make, String model, Integer year, String engine) {}
