package com.example.usermanager.utils.convert;

import com.example.usermanager.domain.entity.Insurance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.util.Collections;
import java.util.List;

public class InsuranceAttributeConverter implements AttributeConverter<List<Insurance>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Insurance> convertToEntityAttribute(String value) {
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
    public String convertToDatabaseColumn(List<Insurance> insurances) {
        try {
            return objectMapper.writeValueAsString(insurances);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot convert List Insurance into Json");
            return null;
        }
    }
}
