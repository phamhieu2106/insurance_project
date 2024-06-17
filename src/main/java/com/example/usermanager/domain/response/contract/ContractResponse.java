package com.example.usermanager.domain.response.contract;

import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.domain.response.insurance.InsuranceResponse;
import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractResponse {

    String id;
    String contractCode;
    Date contractStartDate;
    Date contractEndDate;
    Double contractPayAmount;
    Double contractInsurancePayAmount;
    Double contractNeedPayAmount;
    CustomerResponse customer;
    StatusPayment statusPayment;
    StatusContract statusContract;
    List<InsuranceResponse> insuranceEntities;
}
