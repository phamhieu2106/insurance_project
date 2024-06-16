package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.Insurance;
import com.example.usermanager.domain.request.insurance.InsuranceAddRequest;
import com.example.usermanager.domain.request.insurance.InsuranceUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.insurance.InsuranceResponse;
import com.example.usermanager.repository.InsuranceRepository;
import com.example.usermanager.service.InsuranceService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public InsuranceServiceImpl(InsuranceRepository insuranceRepository, ModelMapper modelMapper) {
        this.insuranceRepository = insuranceRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public WrapperResponse findAll() {
        List<InsuranceResponse> insurances =
                insuranceRepository.findAllBySoftDeleteIsFalse().stream()
                        .map(insurance -> modelMapper.map(insurance, InsuranceResponse.class)).toList();

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), insurances, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse add(InsuranceAddRequest request) {

        if (request == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        if (!validateInsuranceAddRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Insurance insurance = new Insurance();
        insurance.setInsuranceCode(generateInsuranceCode());
        insurance.setInsuranceName(request.getInsuranceName());
        insurance.setTotalPaymentFeeAmount(request.getTotalPaymentFeeAmount());
        insurance.setTotalInsuranceTotalFeeAmount(request.getTotalInsuranceTotalFeeAmount());
        insurance.setCreatedAt(new Date());

        InsuranceResponse insuranceResponse = modelMapper
                .map(insuranceRepository.save(insurance), InsuranceResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.CREATED.getReasonPhrase(), insuranceResponse, HttpStatus.CREATED
        );
    }

    @Override
    public WrapperResponse delete(String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<Insurance> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

        if (insuranceOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Insurance insurance = insuranceOptional.get();
        insurance.setSoftDelete(true);
        insurance.setUpdatedAt(new Date());
        insuranceRepository.save(insurance);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), null, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse update(InsuranceUpdateRequest request, String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        
        if (request == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        if (!validateInsuranceUpdateRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<Insurance> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

        if (insuranceOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Insurance insurance = insuranceOptional.get();
        insurance.setInsuranceName(request.getInsuranceName());
        insurance.setTotalPaymentFeeAmount(request.getTotalPaymentFeeAmount());
        insurance.setTotalInsuranceTotalFeeAmount(request.getTotalInsuranceTotalFeeAmount());
        insurance.setUpdatedAt(new Date());

        InsuranceResponse insuranceResponse = modelMapper
                .map(insuranceRepository.save(insurance), InsuranceResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), insuranceResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse find(String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<Insurance> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

        if (insuranceOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        InsuranceResponse insuranceResponse = modelMapper
                .map(insuranceOptional.get(), InsuranceResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), insuranceResponse, HttpStatus.OK
        );
    }

    private String generateInsuranceCode() {
        long count = insuranceRepository.count();
        String code = String.format("I%03d", count);
        do {
            if (insuranceRepository.existsByInsuranceCode(code)) {
                code = String.format("I%03d", ++count);
            }
        } while (insuranceRepository.existsByInsuranceCode(code));

        return code;
    }

    private boolean validateInsuranceAddRequest(InsuranceAddRequest request) {
        if (request.getInsuranceName() == null || request.getInsuranceName().isEmpty()
                || request.getInsuranceName().isBlank()) {
            return false;
        }
        if (request.getTotalPaymentFeeAmount() == null
                || request.getTotalPaymentFeeAmount().isNaN()
                || request.getTotalPaymentFeeAmount() <= 0) {
            return false;
        }
        if (request.getTotalInsuranceTotalFeeAmount() == null
                || request.getTotalInsuranceTotalFeeAmount().isNaN()
                || request.getTotalInsuranceTotalFeeAmount() <= 0) {
            return false;
        }
        return true;
    }

    private boolean validateInsuranceUpdateRequest(InsuranceUpdateRequest request) {
        if (request.getInsuranceName() == null || request.getInsuranceName().isEmpty()
                || request.getInsuranceName().isBlank()) {
            return false;
        }
        if (request.getTotalPaymentFeeAmount() == null
                || request.getTotalPaymentFeeAmount().isNaN()
                || request.getTotalPaymentFeeAmount() <= 0) {
            return false;
        }
        if (request.getTotalInsuranceTotalFeeAmount() == null
                || request.getTotalInsuranceTotalFeeAmount().isNaN()
                || request.getTotalInsuranceTotalFeeAmount() <= 0) {
            return false;
        }
        return true;
    }
}
