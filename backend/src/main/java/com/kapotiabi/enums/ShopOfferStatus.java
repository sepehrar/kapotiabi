package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ShopOfferStatus {
  PENDING("pending"),
  ACCEPTED("accepted"),
  REJECTED("rejected");

  private final String dbValue;

  ShopOfferStatus(String dbValue) {
    this.dbValue = dbValue;
  }

  @JsonValue
  public String getDbValue() {
    return dbValue;
  }

  @JsonCreator
  public static ShopOfferStatus fromString(String value) {
    for (ShopOfferStatus s : values()) {
      if (s.dbValue.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown shop offer status: " + value);
  }

  public boolean canTransitionTo(ShopOfferStatus next) {
    return switch (this) {
      case PENDING -> next == ACCEPTED || next == REJECTED;
      case ACCEPTED, REJECTED -> false;
    };
  }

  public void assertCanTransitionTo(ShopOfferStatus next) {
    if (!canTransitionTo(next)) {
      throw new IllegalStateException("Shop offer cannot transition from " + this + " to " + next);
    }
  }
}
