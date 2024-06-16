package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.Contract;
import com.example.usermanager.domain.entity.Customer;
import com.example.usermanager.domain.entity.Insurance;
import com.example.usermanager.domain.request.contract.ContractAddRequest;
import com.example.usermanager.domain.request.contract.ContractUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.contract.ContractResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.domain.response.insurance.InsuranceResponse;
import com.example.usermanager.enumeration.StatusContract;
import com.example.usermanager.enumeration.StatusPayment;
import com.example.usermanager.exception.InvalidValueException;
import com.example.usermanager.exception.NotFoundException;
import com.example.usermanager.repository.ContractRepository;
import com.example.usermanager.repository.CustomerRepository;
import com.example.usermanager.repository.InsuranceRepository;
import com.example.usermanager.service.ContractService;
import com.example.usermanager.utils.contraint.DateConstant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final InsuranceRepository insuranceRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ContractServiceImpl(ContractRepository contractRepository
            , InsuranceRepository insuranceRepository, CustomerRepository customerRepository, ModelMapper modelMapper) {
        this.contractRepository = contractRepository;
        this.insuranceRepository = insuranceRepository;
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public WrapperResponse findAll() {

        List<Contract> contracts = contractRepository.findAllBySoftDeleteIsFalse();

        List<ContractResponse> contractResponses = contracts.stream().map(
                contract -> {
                    List<InsuranceResponse> insuranceResponses = contract.getInsurances().stream()
                            .map(insurance -> modelMapper.map(insurance, InsuranceResponse.class)).toList();

                    Optional<Customer> customerOptional = this.customerRepository.findById(contract.getCustomerId());
                    if (customerOptional.isEmpty()) {
                        throw new NotFoundException("Not found Customer by Id: " + contract.getCustomerId());
                    }
                    CustomerResponse customerResponse = modelMapper.map(
                            customerOptional.get(), CustomerResponse.class);

                    ContractResponse contractResponse = modelMapper.map(contract, ContractResponse.class);
                    contractResponse.setCustomer(customerResponse);
                    contractResponse.setInsurances(insuranceResponses);

                    return contractResponse;
                }
        ).toList();


        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponses, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse add(ContractAddRequest request) {

        if (!validateContractAddRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        CustomerResponse customerResponse = modelMapper.map(
                this.customerRepository.findCustomerByIdAndSoftDeleteIsFalse(request.getCustomerId())
                , CustomerResponse.class
        );

        Contract contract = new Contract();
        contract.setContractCode(generateContractCode());
        contract.setCustomerId(request.getCustomerId());
        contract.setContractStartDate(request.getContractStartDate());
        contract.setContractEndDate(request.getContractEndDate());
        contract.setContractTotalPayedAmount(request.getContractTotalPayedAmount());
        contract.setCreatedAt(new Date());

        //money amount
        List<InsuranceResponse> insurances = handleInsuranceContract(request.getInsurancesId());
        double totalContractPayAmount = insurances
                .stream().mapToDouble(InsuranceResponse::getTotalPaymentFeeAmount).sum();
        double totalInsuranceFeeAmount = insurances
                .stream().mapToDouble(InsuranceResponse::getTotalInsuranceTotalFeeAmount).sum();
        double totalNeedPayAmount = totalContractPayAmount - contract.getContractTotalPayedAmount();

        //set total amount
        contract.setContractTotalPayAmount(totalContractPayAmount);
        contract.setContractTotalInsurancePayAmount(totalInsuranceFeeAmount);
        contract.setContractTotalNeedPayAmount(totalNeedPayAmount);

        //set contract payment status
        if (totalNeedPayAmount == totalContractPayAmount) contract.setStatusPayment(StatusPayment.NOT_PAY);
        else if (totalNeedPayAmount == 0) contract.setStatusPayment(StatusPayment.PAYED);
        else contract.setStatusPayment(StatusPayment.PAYED_HALF);

        //set contract status
        long now = DateConstant.convertDateToLong(new Date());
        long contractStartDate = DateConstant.convertDateToLong(contract.getContractStartDate());
        long contractEndDate = DateConstant.convertDateToLong(contract.getContractEndDate());

        if (now >= contractStartDate && now <= contractEndDate) contract.setStatusContract(StatusContract.EFFECTED);
        if (now < contractStartDate) contract.setStatusContract(StatusContract.NOT_EFFECT);


        //convert Contract to ContractResponse
        ContractResponse contractResponse = modelMapper.map(
                this.contractRepository.save(contract), ContractResponse.class);
        contractResponse.setCustomer(customerResponse);
        contractResponse.setInsurances(insurances);

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

        Optional<Contract> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);

        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Contract contract = contractOptional.get();
        contract.setSoftDelete(true);
        this.contractRepository.save(contract);

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

        if (!validateContractUpdateRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<Contract> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);
        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Contract contract = contractOptional.get();

        //check StartDate
        if (DateConstant.convertDateToLong(contract.getContractStartDate())
                > DateConstant.convertDateToLong(request.getContractStartDate())) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (DateConstant.convertDateToLong(request.getContractStartDate())
                != DateConstant.convertDateToLong(contract.getContractStartDate())
                && StatusContract.EFFECTED.equals(contract.getStatusContract())) {

            return WrapperResponse.returnResponse(
                    false, "Contract effected and can not change start date!"
                    , null, HttpStatus.BAD_REQUEST
            );
        }
        if (StatusContract.CANCELLED.equals(contract.getStatusContract())) {
            return WrapperResponse.returnResponse(
                    false, "Contract cancelled!"
                    , null, HttpStatus.BAD_REQUEST
            );
        }
        contract.setContractStartDate(request.getContractStartDate());
        contract.setContractEndDate(request.getContractEndDate());
        contract.setStatusContract(request.getStatusContract());
        contract.setContractTotalPayedAmount(request.getContractTotalPayedAmount());
        contract.setUpdatedAt(new Date());

        List<InsuranceResponse> insurances = handleInsuranceContract(request.getInsurancesId());
        double totalContractPayAmount = insurances
                .stream().mapToDouble(InsuranceResponse::getTotalPaymentFeeAmount).sum();
        double totalInsuranceFeeAmount = insurances
                .stream().mapToDouble(InsuranceResponse::getTotalInsuranceTotalFeeAmount).sum();
        double totalNeedPayAmount = totalContractPayAmount - contract.getContractTotalPayedAmount();


        //set total amount
        contract.setContractTotalPayAmount(totalContractPayAmount);
        contract.setContractTotalInsurancePayAmount(totalInsuranceFeeAmount);
        contract.setContractTotalNeedPayAmount(totalNeedPayAmount);

        //
        if (totalNeedPayAmount == totalContractPayAmount) contract.setStatusPayment(StatusPayment.NOT_PAY);
        else if (totalNeedPayAmount == 0) contract.setStatusPayment(StatusPayment.PAYED);
        else contract.setStatusPayment(StatusPayment.PAYED_HALF);

        //convert Contract to ContractResponse
        CustomerResponse customerResponse = modelMapper.map(
                this.customerRepository.findById(contract.getCustomerId())
                , CustomerResponse.class
        );

        ContractResponse contractResponse = modelMapper.map(
                this.contractRepository.save(contract), ContractResponse.class);
        contractResponse.setCustomer(customerResponse);
        contractResponse.setInsurances(insurances);

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

        Optional<Contract> contractOptional = this.contractRepository.findByIdAndSoftDeleteIsFalse(id);
        if (contractOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Contract contract = contractOptional.get();

        CustomerResponse customerResponse = modelMapper.map(
                this.customerRepository.findById(contract.getCustomerId()).orElse(null)
                , CustomerResponse.class
        );

        List<InsuranceResponse> insurances = contract.getInsurances().stream().map(
                insurance -> modelMapper.map(
                        insurance, InsuranceResponse.class
                )
        ).toList();

        ContractResponse contractResponse = modelMapper.map(contract, ContractResponse.class);
        contractResponse.setCustomer(customerResponse);
        contractResponse.setInsurances(insurances);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), contractResponse, HttpStatus.OK
        );
    }

    private List<InsuranceResponse> handleInsuranceContract(List<String> insuranceIds) {
        return insuranceIds.stream().map(
                id -> {
                    Insurance insurance = this.insuranceRepository.findById(id).orElse(null);
                    if (insurance == null) {
                        throw new InvalidValueException("Null Insurance!");
                    }
                    return modelMapper.map(insurance, InsuranceResponse.class);
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

    private boolean validateContractAddRequest(ContractAddRequest request) {
        if (request == null) return false;

        if (request.getCustomerId() == null
                || request.getCustomerId().isEmpty()
                || request.getCustomerId().isBlank()
                || this.contractRepository.existsByCustomerIdAndStatusContractNot(
                request.getCustomerId(), StatusContract.CANCELLED)) return false;

        if (request.getContractStartDate() == null) return false;
        if (request.getContractEndDate() == null) return false;

        if (request.getInsurancesId().isEmpty()) return false;

        if (DateConstant.convertDateToLong(request.getContractStartDate())
                >= DateConstant.convertDateToLong(request.getContractEndDate())) return false;
        if (DateConstant.convertDateToLong(new Date())
                >= DateConstant.convertDateToLong(request.getContractEndDate())) return false;

        return true;
    }

    private boolean validateContractUpdateRequest(ContractUpdateRequest request) {
        if (request == null) return false;


        if (request.getContractStartDate() == null) return false;
        if (request.getContractEndDate() == null) return false;

        if (request.getInsurancesId().isEmpty()) return false;

        if (DateConstant.convertDateToLong(request.getContractStartDate())
                >= DateConstant.convertDateToLong(request.getContractEndDate())) return false;
        if (DateConstant.convertDateToLong(new Date())
                >= DateConstant.convertDateToLong(request.getContractEndDate())) return false;

        if (request.getStatusContract() == null) return false;

        return true;
    }
}
