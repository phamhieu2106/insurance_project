package com.example.usermanager.domain.response.customer;

import com.example.usermanager.domain.model.Address;
import com.example.usermanager.enumeration.StatusCustomer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    String id;
    String customerName;
    String email;
    String phoneNumber;
    LocalDate dateOfBirth;
    Address address;
    StatusCustomer statusCustomer;
}
