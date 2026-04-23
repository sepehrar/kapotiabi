package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartsQualityConverter implements AttributeConverter<PartsQuality, String> {

  @Override
  public String convertToDatabaseColumn(PartsQuality quality) {
    return quality == null ? null : quality.getValue();
  }

  @Override
  public PartsQuality convertToEntityAttribute(String dbValue) {
    if (dbValue == null) return null;
    return PartsQuality.fromString(dbValue);
  }
}
