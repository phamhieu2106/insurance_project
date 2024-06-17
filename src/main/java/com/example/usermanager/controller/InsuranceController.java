package com.example.usermanager.controller;

import com.example.usermanager.domain.request.insurance.InsuranceAddRequest;
import com.example.usermanager.domain.request.insurance.InsuranceUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.InsuranceService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.InsuranceSpecifications;
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
    public WrapperResponse findAll(
            @RequestParam(name = "pageNumber",
                    required = false, defaultValue = PageConstant.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "pageSize",
                    required = false, defaultValue = PageConstant.PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy",
                    required = false, defaultValue = InsuranceSpecifications.INSURANCE_CODE) String sortBy,
            @RequestParam(name = "sortBy",
                    required = false, defaultValue = PageConstant.PAGE_SORT_TYPE) String sortType,
            @RequestParam(name = "keyword",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String keyword
    ) {
        return this.insuranceService.findAll(pageNumber, pageSize, sortBy, sortType, keyword);
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
