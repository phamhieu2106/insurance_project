package com.example.usermanager.service;

import com.example.usermanager.domain.request.contract.ContractAddRequest;
import com.example.usermanager.domain.request.contract.ContractPageRequest;
import com.example.usermanager.domain.request.contract.ContractUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;

import java.util.Date;

public interface ContractService extends IService<ContractAddRequest, ContractUpdateRequest> {

    WrapperResponse findAllByCustomerId(String customerId);

    void updateContractStatusNotEffect(Date date);

    void updateContractStatusEffected(Date date);

    WrapperResponse cancelContract(String id);

    WrapperResponse findAll(ContractPageRequest request);
}
