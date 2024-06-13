package com.example.usermanager.domain.entity;

import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.enumeration.Gender;
import com.example.usermanager.enumeration.StatusCustomer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    LocalDate dateOfBirth;

    @Embedded
    Address address;

    String jobName;

    String insuranceId;

    @Embedded
    IdentityType proof;

    @Enumerated(EnumType.STRING)
    StatusCustomer statusCustomer;

    Boolean softDelete = false;

    LocalDateTime createdAt;

    String createdBy;

    LocalDateTime updatedAt;

    String lastUpdatedBy;
}
