package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PartsQuality {
  OEM("oem"),
  OEM_EQUIVALENT("oem_equivalent"),
  BUDGET("budget"),
  NOT_SURE("not_sure");

  private final String value;

  PartsQuality(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static PartsQuality fromString(String value) {
    for (PartsQuality q : values()) {
      if (q.value.equalsIgnoreCase(value)) {
        return q;
      }
    }
    throw new IllegalArgumentException("Unknown parts quality: " + value);
  }
}
