package com.kapotiabi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RepairShopRequest(
    @NotBlank String name,
    String address,
    String city,
    String county,
    String phone,
    String email,
    String website,
    String googleMapsUrl,
    String status) {}
