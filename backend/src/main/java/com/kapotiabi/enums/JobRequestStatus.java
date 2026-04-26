package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobRequestStatus {
  PENDING("pending"),
  OFFERS_RECEIVED("offers_received"),
  BOOKED("booked"),
  CANCELLED("cancelled");

  private final String dbValue;

  JobRequestStatus(String dbValue) {
    this.dbValue = dbValue;
  }

  @JsonValue
  public String getDbValue() {
    return dbValue;
  }

  @JsonCreator
  public static JobRequestStatus fromString(String value) {
    for (JobRequestStatus s : values()) {
      if (s.dbValue.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown job request status: " + value);
  }

  public boolean canTransitionTo(JobRequestStatus next) {
    return switch (this) {
      case PENDING -> next == OFFERS_RECEIVED || next == CANCELLED;
      case OFFERS_RECEIVED -> next == BOOKED || next == CANCELLED;
      case BOOKED, CANCELLED -> false;
    };
  }

  public void assertCanTransitionTo(JobRequestStatus next) {
    if (!canTransitionTo(next)) {
      throw new IllegalStateException("Job request cannot transition from " + this + " to " + next);
    }
  }
}
