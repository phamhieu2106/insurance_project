package com.example.usermanager.controller;

import com.example.usermanager.domain.request.insurance.InsuranceAddRequest;
import com.example.usermanager.domain.request.insurance.InsuranceUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insurances")
public class InsuranceController {

    private final InsuranceService insuranceService;

    @Autowired
    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @GetMapping
    public WrapperResponse findAll() {
        return this.insuranceService.findAll();
    }

    @GetMapping("/{id}")
    public WrapperResponse findById(@PathVariable String id) {
        return this.insuranceService.find(id);
    }

    @PostMapping
    public WrapperResponse add(@Valid @RequestBody InsuranceAddRequest request) {
        return this.insuranceService.add(request);
    }

    @PutMapping("/{id}")
    public WrapperResponse update(@Valid @RequestBody InsuranceUpdateRequest request,
                                  @PathVariable String id) {
        return this.insuranceService.update(request, id);
    }

    @DeleteMapping("/{id}")
    public WrapperResponse delete(@PathVariable String id) {
        return this.insuranceService.delete(id);
    }

}
