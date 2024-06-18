package com.example.usermanager.controller;

import com.example.usermanager.domain.request.contract.ContractAddRequest;
import com.example.usermanager.domain.request.contract.ContractPageRequest;
import com.example.usermanager.domain.request.contract.ContractUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    @Autowired
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public WrapperResponse getContracts(
            @RequestBody ContractPageRequest request) {
        return this.contractService.findAll(request);
    }

    @GetMapping("/{id}")
    public WrapperResponse getContractById(@PathVariable String id) {
        return this.contractService.find(id);
    }

    @GetMapping("/find-by-customerId/{id}")
    public WrapperResponse getContractByCustomerId(@PathVariable String id) {
        return this.contractService.findAllByCustomerId(id);
    }

    @PostMapping
    public WrapperResponse addContract(@Valid @RequestBody ContractAddRequest request) {
        return this.contractService.add(request);
    }

    @PutMapping("/{id}")
    public WrapperResponse updateContract(@Valid @RequestBody ContractUpdateRequest request,
                                          @PathVariable String id) {
        return this.contractService.update(request, id);
    }

    @DeleteMapping("/{id}")
    public WrapperResponse deleteContract(@PathVariable String id) {
        return this.contractService.delete(id);
    }

    @DeleteMapping("/cancel-contract/{id}")
    public WrapperResponse cancelContract(@PathVariable String id) {
        System.out.println(".");
        return this.contractService.cancelContract(id);
    }
}
