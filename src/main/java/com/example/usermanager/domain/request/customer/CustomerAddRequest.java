package com.example.usermanager.domain.request.customer;

import com.example.usermanager.domain.entity.Relative;
import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.enumeration.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerAddRequest {

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
    String dateOfBirth;

    @NotNull
    Address address; // address

    String jobName;

    @NotNull
    IdentityType proof;

    @NotNull
    List<Relative> relatives;
}
