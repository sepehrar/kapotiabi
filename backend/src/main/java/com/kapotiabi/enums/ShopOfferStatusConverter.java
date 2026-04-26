package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShopOfferStatusConverter implements AttributeConverter<ShopOfferStatus, String> {

  @Override
  public String convertToDatabaseColumn(ShopOfferStatus status) {
    return status == null ? null : status.getDbValue();
  }

  @Override
  public ShopOfferStatus convertToEntityAttribute(String dbValue) {
    if (dbValue == null) return null;
    return ShopOfferStatus.fromString(dbValue);
  }
}
