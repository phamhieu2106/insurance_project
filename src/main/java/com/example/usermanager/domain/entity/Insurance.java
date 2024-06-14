package com.example.usermanager.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "insurance")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String insuranceCode;

    String insuranceName;

    Double paymentAmount;

    Double insuranceAmount;

    Boolean softDelete = false;

    Date createdAt;

    String createdBy;

    Date updatedAt;

    String lastUpdatedBy;
}
