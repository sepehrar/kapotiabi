package com.kapotiabi.dto.response;

import com.kapotiabi.entity.RepairShopEntity;
import com.kapotiabi.entity.UserSavedShopEntity;
import java.time.LocalDateTime;

public record SavedShopResponse(
    Long shopId,
    String name,
    String city,
    String county,
    String phone,
    String status,
    LocalDateTime savedAt) {

  public static SavedShopResponse from(UserSavedShopEntity saved, RepairShopEntity shop) {
    return new SavedShopResponse(
        shop.getId(),
        shop.getName(),
        shop.getCity(),
        shop.getCounty(),
        shop.getPhone(),
        shop.getStatus(),
        saved.getSavedAt());
  }
}
