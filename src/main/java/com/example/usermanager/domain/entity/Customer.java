package com.example.usermanager.domain.entity;

import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.enumeration.Gender;
import com.example.usermanager.enumeration.StatusCustomer;
import com.example.usermanager.utils.convert.AddressAttributeConverter;
import com.example.usermanager.utils.convert.IdentityTypeAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customer")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String customerCode;

    String customerName;

    @Enumerated(EnumType.STRING)
    Gender gender;

    String phoneNumber;

    String email;

    Date dateOfBirth;

    @Convert(converter = AddressAttributeConverter.class)
    @Column(length = 10000)
    List<Address> addresses;

    String jobName;

    @Convert(converter = IdentityTypeAttributeConverter.class)
    IdentityType proof;

    @Enumerated(EnumType.STRING)
    StatusCustomer statusCustomer;

    Boolean softDelete = false;

    Date createdAt;

    String createdBy;

    Date updatedAt;

    String lastUpdatedBy;
}
