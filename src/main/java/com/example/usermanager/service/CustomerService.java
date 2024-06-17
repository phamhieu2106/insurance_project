package com.example.usermanager.service;

import com.example.usermanager.domain.request.customer.CustomerAddRequest;
import com.example.usermanager.domain.request.customer.CustomerUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;

public interface CustomerService extends IService<CustomerAddRequest, CustomerUpdateRequest> {
    CustomerResponse findCustomerById(String id);

    WrapperResponse findAllCustomer(int pageNumber, int pageSize, String sortBy,
                                    String sortType, String keyword, String statusCustomer);
}
