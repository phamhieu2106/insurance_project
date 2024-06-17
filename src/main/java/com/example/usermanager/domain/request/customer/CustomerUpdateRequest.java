package com.example.usermanager.domain.request.customer;

import com.example.usermanager.domain.entity.RelativeEntity;
import com.example.usermanager.domain.model.AddressModel;
import com.example.usermanager.domain.model.IdentityModel;
import com.example.usermanager.enumeration.Gender;
import com.example.usermanager.enumeration.StatusCustomer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {

    @NotNull
    @NotBlank
    String customerName;

    @NotNull
    Gender gender;

    @NotNull
    @NotBlank
    String phoneNumber;

    @NotNull
    @NotBlank
    String email;

    @NotNull
    Date dateOfBirth;

    @NotNull
    List<AddressModel> addressModels;

    String jobName;

    @NotNull
    IdentityModel proof;

    StatusCustomer statusCustomer;

    List<RelativeEntity> relativeEntities;
}
