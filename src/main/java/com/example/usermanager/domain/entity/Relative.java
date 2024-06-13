package com.example.usermanager.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "relative")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Relative {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String relativeName;

    Integer age;

    String jobName;

    String customerId;

    Boolean softDelete = false;

    LocalDateTime createdAt;

    String createdBy;

    LocalDateTime updatedAt;

    String lastUpdatedBy;
}
