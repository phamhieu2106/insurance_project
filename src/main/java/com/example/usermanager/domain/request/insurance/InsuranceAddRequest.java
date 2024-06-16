package com.example.usermanager.domain.request.insurance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsuranceAddRequest {
    String insuranceName;
    Double totalPaymentFeeAmount;
    Double totalInsuranceTotalFeeAmount;
}
