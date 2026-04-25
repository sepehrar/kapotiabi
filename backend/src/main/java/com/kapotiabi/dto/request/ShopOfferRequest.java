package com.kapotiabi.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopOfferRequest(
    Long shopId,
    @NotNull BigDecimal priceEstimate,
    String breakdown,
    String partsTier,
    BigDecimal estimatedDurationHours,
    LocalDateTime validUntil) {}
