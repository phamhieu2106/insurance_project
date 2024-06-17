package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.ContractEntity;
import com.example.usermanager.domain.entity.CustomerEntity;
import com.example.usermanager.domain.entity.InsuranceEntity;
import com.example.usermanager.domain.request.contract.ContractAddRequest;
import com.example.usermanager.domain.request.contract.ContractUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.contract.ContractResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.domain.response.insurance.InsuranceResponse;
import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import com.example.usermanager.exception.NotFoundException;
import com.example.usermanager.repository.ContractRepository;
import com.example.usermanager.repository.CustomerRepository;
import com.example.usermanager.repository.InsuranceRepository;
import com.example.usermanager.service.ContractService;
import com.example.usermanager.utils.contraint.DateConstant;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.ContractSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final InsuranceRepository insuranceRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public WrapperResponse findAll(int pageNumber, int pageSize, String sortBy, String sortType, String keyword,
                                   String statusPayment, String statusContract) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, PageConstant.getSortBy(sortBy, sortType));
        Specification<ContractEntity> spec = ContractSpecifications
                .withKeywordAndStatus(keyword, statusPayment, statusContract);
        Page<ContractEntity> contracts = contractRepository.findAll(spec, pageable);

        List<ContractResponse> contractResponses = contracts.stream().map(
                contractEntity -> {
                    List<InsuranceResponse> insuranceResponses = contractEntity.getInsuranceEntities().stream()
                            .map(insurance -> modelMapper.map(insurance, InsuranceResponse.class)).toList();

                    Optional<CustomerEntity> customerOptional = this.customerRepository.findById(contractEntity.getCustomerId());
                    if (customerOptional.isEmpty()) {
                        throw new NotFoundException("Not found Customer by Id: " + contractEntity.getCustomerId());
                    }
                    CustomerResponse customerResponse = modelMapper.map(
                            customerOptional.get(), CustomerResponse.class);

                    ContractResponse contractResponse = modelMapper.map(contractEntity, ContractResponse.class);
                    contractResponse.setCustomer(customerResponse);
                    contractResponse.setInsuranceEntities(insuranceResponses);

                    return contractResponse;
                }
        ).toList();

        Page<ContractResponse> pageResponse = new PageImpl<>(
                contractResponses, pageable, contracts.getTotalElements()
        );

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), pageResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse add(ContractAddRequest request) {

        if (!isValidAddRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setContractCode(generateContractCode());
        contractEntity.setCustomerId(request.getCustomerId());
        contractEntity.setContractStartDate(request.getContractStartDate());
        contractEntity.setContractEndDate(request.getContractEndDate());
        contractEntity.setContractTotalPayedAmount(request.getContractTotalPayedAmount());
        contractEntity.setCreatedAt(new Date());

        //set contract status
        Date now = new Date();
        if (DateConstant.isDate1BeforeDate2(contractEntity.getContractStartDate(), now))
            contractEntity.setStatusContract(StatusContract.NOT_EFFECT);
        if (DateConstant.isDate1AfterDate2(contractEntity.getContractStartDate(), now)
                && DateConstant.isDate1BeforeDate2(now, contractEntity.getContractEndDate()))
            contractEntity.setStatusContract(StatusContract.EFFECTED);
        if (DateConstant.isDate1AfterDate2(now, contractEntity.getContractEndDate()))
            contractEntity.setStatusContract(StatusContract.END_EFFECTED);

        //money amount
        List<InsuranceEntity> insuranceEntities = handleGetInsurance(request.getInsurancesId());
        double totalContractPayAmount = insuranceEntities
                .stream().mapToDouble(InsuranceEntity::getTotalPaymentFeeAmount).sum();
        double totalInsuranceFeeAmount = insuranceEntities
                .stream().mapToDouble(InsuranceEntity::getTotalInsuranceTotalFeeAmount).sum();
        double totalNeedPayAmount = totalInsuranceFeeAmount - contractEntity.getContractTotalPayedAmount();
        if (totalNeedPayAmount <= 0) {
            totalNeedPayAmount = 0;
            contractEntity.setContractTotalPayedAmount(totalInsuranceFeeAmount);
        }

        //set insurance
        contractEntity.setInsuranceEntities(insuranceEntities);
        //set total amount
        contractEntity.setContractTotalPayAmount(totalContractPayAmount);
        contractEntity.setContractTotalInsurancePayAmount(totalInsuranceFeeAmount);
        contractEntity.setContractTotalNeedPayAmount(totalNeedPayAmount);

        //set contract payment status
        if (totalNeedPayAmount == totalInsuranceFeeAmount) contractEntity.setStatusPayment(StatusPayment.NOT_PAY);
        else if (totalNeedPayAmount == 0) contractEntity.setStatusPayment(StatusPayment.PAYED);
        else contractEntity.setStatusPayment(StatusPayment.PAYED_HALF);


        //CustomerResponse
        CustomerEntity customerEntity = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(
                request.getCustomerId()).orElse(null);
        CustomerResponse customerResponse = modelMapper.map(
                customerEntity
                , CustomerResponse.class
        );

        //convert Contract to ContractResponse
        ContractResponse contractResponse = modelMapper.map(
                this.contractRepository.save(contractEntity), ContractResponse.class);
        contractResponse.setCustomer(customerResponse);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse delete(String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<ContractEntity> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);

        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        ContractEntity contractEntity = contractOptional.get();
        contractEntity.setSoftDelete(true);
        this.contractRepository.save(contractEntity);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), null, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse update(ContractUpdateRequest request, String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        if (!isValidUpdateRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<ContractEntity> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);
        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        ContractEntity contractEntity = contractOptional.get();

        //check Date
        if (DateConstant.convertDateToLong(request.getContractStartDate())
                != DateConstant.convertDateToLong(contractEntity.getContractStartDate())
                && StatusContract.EFFECTED.equals(contractEntity.getStatusContract())) {

            return WrapperResponse.returnResponse(
                    false, "Contract effected and can not change start date!"
                    , null, HttpStatus.BAD_REQUEST
            );
        }
        if (StatusContract.CANCELLED.equals(contractEntity.getStatusContract())) {
            return WrapperResponse.returnResponse(
                    false, "Contract cancelled!"
                    , null, HttpStatus.BAD_REQUEST
            );
        }
        contractEntity.setContractStartDate(request.getContractStartDate());
        contractEntity.setContractEndDate(request.getContractEndDate());
        contractEntity.setContractTotalPayedAmount(request.getContractTotalPayedAmount());
        contractEntity.setUpdatedAt(new Date());

        //set contract status
        Date now = new Date();
        if (DateConstant.isDate1BeforeDate2(contractEntity.getContractStartDate(), now))
            contractEntity.setStatusContract(StatusContract.NOT_EFFECT);
        if (DateConstant.isDate1AfterDate2(contractEntity.getContractStartDate(), now)
                && DateConstant.isDate1BeforeDate2(now, contractEntity.getContractEndDate()))
            contractEntity.setStatusContract(StatusContract.EFFECTED);
        if (DateConstant.isDate1AfterDate2(now, contractEntity.getContractEndDate()))
            contractEntity.setStatusContract(StatusContract.END_EFFECTED);

        //money amount
        List<InsuranceEntity> insuranceEntities = handleGetInsurance(request.getInsurancesId());
        double totalContractPayAmount = insuranceEntities
                .stream().mapToDouble(InsuranceEntity::getTotalPaymentFeeAmount).sum();
        double totalInsuranceFeeAmount = insuranceEntities
                .stream().mapToDouble(InsuranceEntity::getTotalInsuranceTotalFeeAmount).sum();
        double totalNeedPayAmount = totalInsuranceFeeAmount - contractEntity.getContractTotalPayedAmount();
        if (totalNeedPayAmount <= 0) {
            totalNeedPayAmount = 0;
            contractEntity.setContractTotalPayedAmount(totalInsuranceFeeAmount);
        }

        //set insurance
        contractEntity.setInsuranceEntities(insuranceEntities);
        //set total amount
        contractEntity.setContractTotalPayAmount(totalContractPayAmount);
        contractEntity.setContractTotalInsurancePayAmount(totalInsuranceFeeAmount);
        contractEntity.setContractTotalNeedPayAmount(totalNeedPayAmount);

        //set contract payment status
        if (totalNeedPayAmount == totalInsuranceFeeAmount) contractEntity.setStatusPayment(StatusPayment.NOT_PAY);
        else if (totalNeedPayAmount == 0) contractEntity.setStatusPayment(StatusPayment.PAYED);
        else contractEntity.setStatusPayment(StatusPayment.PAYED_HALF);


        //CustomerResponse
        CustomerEntity customerEntity = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(
                request.getCustomerId()).orElse(null);
        CustomerResponse customerResponse = modelMapper.map(
                customerEntity
                , CustomerResponse.class
        );

        //convert Contract to ContractResponse
        ContractResponse contractResponse = modelMapper.map(
                this.contractRepository.save(contractEntity), ContractResponse.class);
        contractResponse.setCustomer(customerResponse);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse find(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<ContractEntity> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);
        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        ContractEntity contractEntity = contractOptional.get();

        CustomerResponse customerResponse = modelMapper.map(
                this.customerRepository.findById(contractEntity.getCustomerId()).orElse(null)
                , CustomerResponse.class
        );

        ContractResponse contractResponse = modelMapper.map(contractEntity, ContractResponse.class);
        contractResponse.setCustomer(customerResponse);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse findAllByCustomerId(String customerId) {
        if (customerId == null || customerId.isEmpty() || customerId.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        List<ContractEntity> contractEntities = this.contractRepository
                .findAllByCustomerIdAndSoftDeleteIsFalse(customerId);

        if (contractEntities.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }


        CustomerResponse customerResponse = modelMapper.map(
                this.customerRepository.findById(customerId).orElse(null), CustomerResponse.class);

        List<ContractResponse> contractResponses = contractEntities.stream().map(
                contractEntity -> {
                    ContractResponse response = modelMapper.map(contractEntity, ContractResponse.class);
                    response.setCustomer(customerResponse);
                    return response;
                }
        ).toList();

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponses, HttpStatus.OK
        );
    }

    @Override
    public void updateContractStatus(Date date) {
        this.contractRepository.updateStatusContract(date);
    }

    @Override
    public WrapperResponse cancelContract(String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }


        Optional<ContractEntity> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);
        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }
        ContractEntity contractEntity = contractOptional.get();
        contractEntity.setStatusContract(StatusContract.CANCELLED);
        this.contractRepository.save(contractEntity);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), null, HttpStatus.OK
        );
    }

    private List<InsuranceEntity> handleGetInsurance(List<String> insuranceIds) {
        return insuranceIds.stream().map(
                id -> {
                    InsuranceEntity insuranceEntity = this.insuranceRepository.findById(id).orElse(null);
                    if (insuranceEntity == null) {
                        throw new NotFoundException("Null Insurance!");
                    }
                    return insuranceEntity;
                }
        ).toList();
    }

    private String generateContractCode() {
        long count = contractRepository.count();
        String code;
        do {
            code = String.format("CT%03d", count);
            if (contractRepository.existsByContractCode(code)) {
                code = String.format("CT%03d", ++count);
            }
        } while (contractRepository.existsByContractCode(code));

        return code;
    }

    private boolean isValidAddRequest(ContractAddRequest request) {
        if (request == null) return false;

        if (request.getCustomerId() == null
                || request.getCustomerId().isEmpty()
                || request.getCustomerId().isBlank()
                || !this.customerRepository.existsById(request.getCustomerId())) return false;

        if (request.getContractStartDate() == null) return false;
        if (request.getContractEndDate() == null) return false;

        if (DateConstant.isDate1AfterDate2(request.getContractStartDate(), request.getContractEndDate())) return false;

        if (request.getInsurancesId().isEmpty()) return false;

        return true;
    }

    private boolean isValidUpdateRequest(ContractUpdateRequest request) {
        if (request == null) return false;


        if (request.getCustomerId() == null
                || request.getCustomerId().isEmpty()
                || request.getCustomerId().isBlank()
                || !this.customerRepository.existsById(request.getCustomerId())) return false;

        if (request.getContractStartDate() == null) return false;
        if (request.getContractEndDate() == null) return false;

        if (DateConstant.isDate1AfterDate2(request.getContractStartDate(), request.getContractEndDate())) return false;

        if (request.getInsurancesId().isEmpty()) return false;


        return true;
    }

}
