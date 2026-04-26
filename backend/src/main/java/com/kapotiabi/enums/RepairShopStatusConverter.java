package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RepairShopStatusConverter implements AttributeConverter<RepairShopStatus, String> {

  @Override
  public String convertToDatabaseColumn(RepairShopStatus status) {
    return status == null ? null : status.getDbValue();
  }

  @Override
  public RepairShopStatus convertToEntityAttribute(String dbValue) {
    if (dbValue == null) return null;
    return RepairShopStatus.fromString(dbValue);
  }
}
