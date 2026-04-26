package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

  @Override
  public String convertToDatabaseColumn(BookingStatus status) {
    return status == null ? null : status.getDbValue();
  }

  @Override
  public BookingStatus convertToEntityAttribute(String dbValue) {
    if (dbValue == null) return null;
    return BookingStatus.fromString(dbValue);
  }
}
