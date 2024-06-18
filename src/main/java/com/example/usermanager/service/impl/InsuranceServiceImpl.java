package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.InsuranceEntity;
import com.example.usermanager.domain.request.insurance.InsuranceAddRequest;
import com.example.usermanager.domain.request.insurance.InsurancePageRequest;
import com.example.usermanager.domain.request.insurance.InsuranceUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.insurance.InsuranceResponse;
import com.example.usermanager.repository.InsuranceRepository;
import com.example.usermanager.service.InsuranceService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.InsuranceSpecifications;
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
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final ModelMapper modelMapper;

    @Override
    public WrapperResponse findAll(InsurancePageRequest request) {

        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(),
                PageConstant.getSortBy(request.getSortBys(), request.getSortOrder()));
        Specification<InsuranceEntity> spec = InsuranceSpecifications.withSpec(request.getKeyword());
        Page<InsuranceEntity> entityPage = this.insuranceRepository.findAll(spec, pageable);

        List<InsuranceResponse> insurances = entityPage.stream()
                .map(insurance -> modelMapper.map(insurance, InsuranceResponse.class)).toList();

        //        Create Response Page
        Page<InsuranceResponse> responsePage = new PageImpl<>(
                insurances, pageable, entityPage.getTotalElements());

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), responsePage, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse add(InsuranceAddRequest request) {

        if (request == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        if (!isValidAddRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        InsuranceEntity insuranceEntity = new InsuranceEntity();
        insuranceEntity.setInsuranceCode(generateInsuranceCode());
        insuranceEntity.setInsuranceName(request.getInsuranceName());
        insuranceEntity.setTotalPaymentFeeAmount(request.getTotalPaymentFeeAmount());
        insuranceEntity.setTotalInsuranceTotalFeeAmount(request.getTotalInsuranceTotalFeeAmount());
        insuranceEntity.setCreatedAt(new Date());

        InsuranceResponse insuranceResponse = modelMapper
                .map(insuranceRepository.save(insuranceEntity), InsuranceResponse.class);

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

        Optional<InsuranceEntity> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

        if (insuranceOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        InsuranceEntity insuranceEntity = insuranceOptional.get();
        insuranceEntity.setSoftDelete(true);
        insuranceEntity.setUpdatedAt(new Date());
        insuranceRepository.save(insuranceEntity);

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

        if (!isValidUpdateRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<InsuranceEntity> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

        if (insuranceOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        InsuranceEntity insuranceEntity = insuranceOptional.get();
        insuranceEntity.setInsuranceName(request.getInsuranceName());
        insuranceEntity.setTotalPaymentFeeAmount(request.getTotalPaymentFeeAmount());
        insuranceEntity.setTotalInsuranceTotalFeeAmount(request.getTotalInsuranceTotalFeeAmount());
        insuranceEntity.setUpdatedAt(new Date());

        InsuranceResponse insuranceResponse = modelMapper
                .map(insuranceRepository.save(insuranceEntity), InsuranceResponse.class);

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

        Optional<InsuranceEntity> insuranceOptional = insuranceRepository.findByIdAndSoftDeleteIsFalse(id);

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

    private boolean isValidAddRequest(InsuranceAddRequest request) {
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

    private boolean isValidUpdateRequest(InsuranceUpdateRequest request) {
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
