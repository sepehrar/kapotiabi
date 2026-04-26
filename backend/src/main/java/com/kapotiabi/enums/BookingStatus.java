package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookingStatus {
  AWAITING_SHOP("awaiting_shop"),
  CONFIRMED("confirmed"),
  COMPLETED("completed"),
  CANCELLED("cancelled");

  private final String dbValue;

  BookingStatus(String dbValue) {
    this.dbValue = dbValue;
  }

  @JsonValue
  public String getDbValue() {
    return dbValue;
  }

  @JsonCreator
  public static BookingStatus fromString(String value) {
    for (BookingStatus s : values()) {
      if (s.dbValue.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown booking status: " + value);
  }

  public boolean canTransitionTo(BookingStatus next) {
    return switch (this) {
      case AWAITING_SHOP -> next == CONFIRMED || next == CANCELLED;
      case CONFIRMED -> next == COMPLETED || next == CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };
  }

  public void assertCanTransitionTo(BookingStatus next) {
    if (!canTransitionTo(next)) {
      throw new IllegalStateException("Booking cannot transition from " + this + " to " + next);
    }
  }
}
