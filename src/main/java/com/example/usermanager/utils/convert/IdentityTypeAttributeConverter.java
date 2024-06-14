package com.example.usermanager.utils.convert;

import com.example.usermanager.domain.model.IdentityType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

public class IdentityTypeAttributeConverter implements AttributeConverter<IdentityType, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(IdentityType identityType) {
        try {
            return objectMapper.writeValueAsString(identityType);
        } catch (JsonProcessingException jpe) {
            System.out.println("Cannot convert IdentityType into JSON");
            return null;
        }
    }

    @Override
    public IdentityType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, IdentityType.class);
        } catch (Exception e) {
            System.out.println("Cannot convert JSON into IdentityType");
            return null;
        }
    }
}
