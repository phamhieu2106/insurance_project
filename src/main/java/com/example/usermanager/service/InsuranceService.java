package com.example.usermanager.service;

import com.example.usermanager.domain.request.insurance.InsuranceAddRequest;
import com.example.usermanager.domain.request.insurance.InsuranceUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;

public interface InsuranceService extends IService<InsuranceAddRequest, InsuranceUpdateRequest> {
    WrapperResponse findAll(int pageNumber, int pageSize, String sortBy, String sortType, String keyword);
}
