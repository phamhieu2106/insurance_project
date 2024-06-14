package com.example.usermanager.domain.entity;

import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;

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

    Date contractStartDate;

    Date contractEndDate;

    Double contractPayAmount;

    Double contractInsurancePayAmount;

    Double contractNeedPayAmount;

    Double contractPayedAmount;

    String customerId;


    @Enumerated(EnumType.STRING)
    StatusPayment statusPayment;

    @Enumerated(EnumType.STRING)
    StatusContract statusContract;

    Boolean softDelete = false;

    Date createdAt;

    String createdBy;

    Date updatedAt;

    String lastUpdatedBy;

}
