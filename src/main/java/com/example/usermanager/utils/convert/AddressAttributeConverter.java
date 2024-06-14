package com.example.usermanager.utils.convert;

import com.example.usermanager.domain.model.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.util.Collections;
import java.util.List;


public class AddressAttributeConverter implements AttributeConverter<List<Address>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(List<Address> addresses) {
        try {
            return objectMapper.writeValueAsString(addresses);
        } catch (JsonProcessingException jpe) {
            System.out.println("Cannot convert Address into JSON");
            return null;
        }
    }

    @Override
    public List<Address> convertToEntityAttribute(String value) {
        if (value == null) {
            return Collections.emptyList();
        }

        try {
            // Sử dụng ObjectMapper để chuyển đổi chuỗi JSON thành danh sách đối tượng Address
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception e) {
            System.out.println("Cannot convert JSON into Address");
            return Collections.emptyList();
        }
    }
}
