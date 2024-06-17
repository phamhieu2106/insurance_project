package com.example.usermanager.domain.request.contract;

import jakarta.validation.constraints.Min;
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
public class ContractAddRequest {

    @NotNull
    @NotBlank
    String customerId;

    @NotNull
    Date contractStartDate;
    @NotNull
    Date contractEndDate;

    @NotNull
    @Min(value = 0)
    Double contractTotalPayedAmount;

    @NotNull
    List<String> insurancesId;

}
