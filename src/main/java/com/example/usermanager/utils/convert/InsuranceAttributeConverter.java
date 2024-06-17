package com.example.usermanager.utils.convert;

import com.example.usermanager.domain.entity.InsuranceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.util.Collections;
import java.util.List;

public class InsuranceAttributeConverter implements AttributeConverter<List<InsuranceEntity>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<InsuranceEntity> convertToEntityAttribute(String value) {
        if (value == null) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception e) {
            System.out.println("Cannot convert JSON into List Insurance");
            return Collections.emptyList();
        }
    }

    @Override
    public String convertToDatabaseColumn(List<InsuranceEntity> insuranceEntities) {
        try {
            return objectMapper.writeValueAsString(insuranceEntities);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot convert List Insurance into Json");
            return null;
        }
    }
}
