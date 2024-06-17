package com.example.usermanager.domain.request.insurance;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsuranceUpdateRequest {

    @NotNull
    @NotBlank
    String insuranceName;

    @NotNull
    @Min(value = 0)
    Double totalPaymentFeeAmount;

    @NotNull
    @Min(value = 0)
    Double totalInsuranceTotalFeeAmount;
}
