package com.example.usermanager.domain.request.customer;

import com.example.usermanager.domain.entity.Relative;
import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.enumeration.Gender;
import com.example.usermanager.enumeration.StatusCustomer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty
    String customerName;

    @NotNull
    Gender gender;

    @NotNull
    @NotBlank
    @NotEmpty
    String phoneNumber;

    @NotNull
    @NotBlank
    @NotEmpty
    String email;

    @NotNull
    Date dateOfBirth;

    @NotNull
    List<Address> addresses;

    String jobName;

    @NotNull
    IdentityType proof;

    StatusCustomer statusCustomer;

    List<Relative> relatives;
}
