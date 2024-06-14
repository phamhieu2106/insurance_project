package com.example.usermanager.controller;

import com.example.usermanager.domain.request.customer.CustomerAddRequest;
import com.example.usermanager.domain.request.customer.CustomerUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public WrapperResponse findAll() {
        return this.customerService.findAll();
    }

    @GetMapping("/{id}")
    public WrapperResponse findById(@PathVariable String id) {
        CustomerResponse customerResponse = this.customerService.findCustomerById(id);
        if (customerResponse == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }
        return WrapperResponse.returnResponse(
                true, HttpStatus.FOUND.getReasonPhrase(), customerResponse, HttpStatus.FOUND
        );
    }

    @PostMapping
    public WrapperResponse addCustomer(@Valid @RequestBody CustomerAddRequest request) {
        return this.customerService.add(request);
    }

    @PutMapping("/{id}")
    public WrapperResponse updateCustomer(@PathVariable String id
            , @Valid @RequestBody CustomerUpdateRequest request) {
        return this.customerService.update(request, id);
    }

    @DeleteMapping("/{id}")
    public WrapperResponse deleteCustomer(@PathVariable String id) {
        return this.customerService.delete(id);
    }

}
