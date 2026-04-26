package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JobRequestStatusConverter implements AttributeConverter<JobRequestStatus, String> {

  @Override
  public String convertToDatabaseColumn(JobRequestStatus status) {
    return status == null ? null : status.getDbValue();
  }

  @Override
  public JobRequestStatus convertToEntityAttribute(String dbValue) {
    if (dbValue == null) return null;
    return JobRequestStatus.fromString(dbValue);
  }
}
