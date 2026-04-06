package com.kapotiabi.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.getDbValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return UserRole.fromString(dbValue);
    }
}
