package com.kapotiabi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    CAR_OWNER("car_owner"),
    SHOP_OWNER("shop_owner"),
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static UserRole fromString(String value) {
        for (UserRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
