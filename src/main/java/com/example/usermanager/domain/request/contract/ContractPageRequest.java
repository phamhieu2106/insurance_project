package com.example.usermanager.domain.request.contract;

import com.example.usermanager.domain.request.PageRequest;
import com.example.usermanager.utils.contraint.PageConstant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractPageRequest extends PageRequest {
    String statusPayment = PageConstant.PAGE_DEFAULT_VALUE;
    String statusContract = PageConstant.PAGE_DEFAULT_VALUE;
}
