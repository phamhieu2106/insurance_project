package com.example.usermanager.controller;

import com.example.usermanager.domain.request.contract.ContractAddRequest;
import com.example.usermanager.domain.request.contract.ContractUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.ContractService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.ContractSpecifications;
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
            @RequestParam(name = "pageNumber",
                    required = false, defaultValue = PageConstant.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "pageSize",
                    required = false, defaultValue = PageConstant.PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy",
                    required = false, defaultValue = ContractSpecifications.CONTRACT_CODE) String sortBy,
            @RequestParam(name = "sortType",
                    required = false, defaultValue = PageConstant.PAGE_SORT_TYPE) String sortType,
            @RequestParam(name = "keyword",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String keyword,
            @RequestParam(name = "statusPayment",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String statusPayment,
            @RequestParam(name = "statusContract",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String statusContract
    ) {
        return this.contractService.findAll(pageNumber, pageSize, sortBy, sortType, keyword, statusPayment, statusContract);
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
