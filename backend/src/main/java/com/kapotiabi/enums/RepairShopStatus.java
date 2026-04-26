package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RepairShopStatus {
  DRAFT("draft"),
  ACTIVE("active"),
  REJECTED("rejected");

  private final String dbValue;

  RepairShopStatus(String dbValue) {
    this.dbValue = dbValue;
  }

  @JsonValue
  public String getDbValue() {
    return dbValue;
  }

  @JsonCreator
  public static RepairShopStatus fromString(String value) {
    for (RepairShopStatus s : values()) {
      if (s.dbValue.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown repair shop status: " + value);
  }

  public boolean canTransitionTo(RepairShopStatus next) {
    return switch (this) {
      case DRAFT -> next == ACTIVE || next == REJECTED;
      case ACTIVE, REJECTED -> false;
    };
  }

  public void assertCanTransitionTo(RepairShopStatus next) {
    if (!canTransitionTo(next)) {
      throw new IllegalStateException("Repair shop cannot transition from " + this + " to " + next);
    }
  }
}
