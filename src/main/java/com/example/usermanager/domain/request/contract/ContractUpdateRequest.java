package com.example.usermanager.domain.request.contract;

import com.example.usermanager.enumeration.StatusContract;
import jakarta.validation.constraints.Min;
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
public class ContractUpdateRequest {

    @NotNull
    Date contractStartDate;

    @NotNull
    Date contractEndDate;

    @NotNull
    List<String> insurancesId;

    @NotNull
    @Min(value = 0)
    Double contractTotalPayedAmount;

    StatusContract statusContract;

}
