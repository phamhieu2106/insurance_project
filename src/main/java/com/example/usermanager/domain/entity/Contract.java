package com.example.usermanager.domain.entity;

import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contract")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String contractCode;

    Long customerId;

    LocalDateTime contractStartDate;

    LocalDateTime contractEndDate;

    Double contractPayAmount;

    Double contractInsurancePayAmount;

    Double contractNeedPayAmount;

    Double contractPayedAmount;

    @Enumerated(EnumType.STRING)
    StatusPayment statusPayment;

    @Enumerated(EnumType.STRING)
    StatusContract statusContract;

    Boolean softDelete = false;

    LocalDateTime createdAt;

    String createdBy;

    LocalDateTime updatedAt;

    String lastUpdatedBy;

}
