package com.example.usermanager.domain.entity;

import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import com.example.usermanager.utils.convert.InsuranceAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

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

    Double contractTotalPayAmount;

    Double contractTotalInsurancePayAmount;

    Double contractTotalNeedPayAmount;

    Double contractTotalPayedAmount;

    String customerId;

    @Convert(converter = InsuranceAttributeConverter.class)
    @Column(length = 10000)
    List<Insurance> insurances;

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
