package com.example.usermanager.domain.response.customer;

import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.response.relative.RelativeResponse;
import com.example.usermanager.enumeration.StatusCustomer;
import com.example.usermanager.utils.contraint.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    String id;
    String customerName;
    String email;
    String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.DATE_PATTERN)
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    Date dateOfBirth;

    List<Address> addresses;

    List<RelativeResponse> relatives;

    StatusCustomer statusCustomer;
}
