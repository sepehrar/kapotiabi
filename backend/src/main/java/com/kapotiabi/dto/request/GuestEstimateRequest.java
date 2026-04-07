package com.kapotiabi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GuestEstimateRequest(
    @NotBlank String description, String plateNumber, String city, String county) {}
