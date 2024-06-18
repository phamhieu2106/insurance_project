package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.CustomerEntity;
import com.example.usermanager.domain.entity.RelativeEntity;
import com.example.usermanager.domain.model.AddressModel;
import com.example.usermanager.domain.model.IdentityModel;
import com.example.usermanager.domain.request.customer.CustomerAddRequest;
import com.example.usermanager.domain.request.customer.CustomerUpdateRequest;
import com.example.usermanager.domain.request.customer.PageCustomerRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.domain.response.relative.RelativeResponse;
import com.example.usermanager.enumeration.StatusCustomer;
import com.example.usermanager.exception.InvalidValueException;
import com.example.usermanager.repository.CustomerRepository;
import com.example.usermanager.repository.RelativeRepository;
import com.example.usermanager.service.CustomerService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.contraint.RegexConstant;
import com.example.usermanager.utils.specific.CustomerSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final String CUSTOMER_VALUE_NAME = "customerResponse";
    private final String CUSTOMER_KEY_NAME = "customer_";

    private final CustomerRepository customerRepository;
    private final RelativeRepository relativeRepository;
    private final ModelMapper modelMapper;
    private final CacheManager cacheManager;


    @Override
    public WrapperResponse findAllCustomer(PageCustomerRequest request) {

//        Get Page
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize()
                , PageConstant.getSortBy(request.getSortBys(), request.getSortOrder()));
        Specification<CustomerEntity> spec = CustomerSpecifications.withKeywordAndStatus(
                request.getKeyword(), request.getStatusCustomer());
        Page<CustomerEntity> customerEntityPage = this.customerRepository.findAll(spec, pageable);

//        Map to List Customer Response
        List<CustomerResponse> customerResponses = customerEntityPage.stream()
                .map(customerEntity -> {
                    List<RelativeResponse> relatives =
                            this.relativeRepository.findAllByCustomerIdAndSoftDeleteIsFalse(customerEntity.getId())
                                    .stream().map(
                                            relativeEntity -> modelMapper.map(relativeEntity, RelativeResponse.class)
                                    ).toList();
                    CustomerResponse customerResponse = this.modelMapper.map(customerEntity, CustomerResponse.class);
                    customerResponse.setRelativeEntities(relatives);
                    return customerResponse;
                }).toList();


//        Create Response Page
        Page<CustomerResponse> responsePage = new PageImpl<>(
                customerResponses, pageable, customerEntityPage.getTotalElements());

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), responsePage, HttpStatus.OK
        );
    }


    @Override
    @Transactional(rollbackOn = InvalidValueException.class)
    public WrapperResponse add(CustomerAddRequest request) {

        if (!isValidAddRequest(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setDateOfBirth(request.getDateOfBirth());
        customerEntity.setCustomerCode(generateCustomerCode());
        customerEntity.setCustomerName(request.getCustomerName());
        customerEntity.setGender(request.getGender());
        customerEntity.setPhoneNumber(request.getPhoneNumber());
        customerEntity.setEmail(request.getEmail());
        customerEntity.setAddressModels(request.getAddressModels());
        customerEntity.setProof(request.getProof());
        customerEntity.setJobName(request.getJobName());
        customerEntity.setStatusCustomer(StatusCustomer.POTENTIAL);
        customerEntity.setCreatedAt(new Date());

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customerEntity), CustomerResponse.class);

        List<RelativeResponse> relativeResponses = createRelatives(request, customerResponse.getId());
        customerResponse.setRelativeEntities(relativeResponses);

        return WrapperResponse.returnResponse(
                true, HttpStatus.CREATED.getReasonPhrase(), customerResponse, HttpStatus.CREATED
        );
    }

    @Override
    public WrapperResponse delete(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<CustomerEntity> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        CustomerEntity customerEntity = customerOptional.get();
        customerEntity.setSoftDelete(true);

        this.customerRepository.save(customerEntity);

        return WrapperResponse.returnResponse(
                true, HttpStatus.NO_CONTENT.getReasonPhrase(), null, HttpStatus.NO_CONTENT
        );
    }

    @Override
    public WrapperResponse update(CustomerUpdateRequest request, String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<CustomerEntity> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        CustomerEntity customerEntity = customerOptional.get();
        if (!isValidUpdateRequest(customerEntity, request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        customerEntity.setDateOfBirth(request.getDateOfBirth());
        customerEntity.setCustomerCode(customerEntity.getCustomerCode());
        customerEntity.setCustomerName(request.getCustomerName());
        customerEntity.setGender(request.getGender());
        customerEntity.setPhoneNumber(request.getPhoneNumber());
        customerEntity.setEmail(request.getEmail());
        customerEntity.setAddressModels(request.getAddressModels());
        customerEntity.setProof(request.getProof());
        customerEntity.setJobName(request.getJobName());
        customerEntity.setStatusCustomer(request.getStatusCustomer());
        customerEntity.setUpdatedAt(new Date());

        // Lấy ID của khách hàng
        clearCustomerResponseCache(customerEntity.getId());

        //tạo key mới
        String newCacheKey = CUSTOMER_KEY_NAME + customerEntity.getId();
        Objects.requireNonNull(cacheManager
                        .getCache(CUSTOMER_VALUE_NAME))
                .put(newCacheKey, modelMapper.map(customerEntity, CustomerResponse.class));

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customerEntity), CustomerResponse.class);

        List<RelativeResponse> relativeResponses = updateRelatives(request, customerResponse.getId());
        customerResponse.setRelativeEntities(relativeResponses);

        return WrapperResponse.returnResponse(
                false, HttpStatus.OK.getReasonPhrase(), customerResponse, HttpStatus.OK
        );
    }

    @Cacheable(value = CUSTOMER_VALUE_NAME, key = "'customer_' + #id")
    public CustomerResponse findCustomerById(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return null;
        }

        Optional<CustomerEntity> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);
        return customerOptional.map(customerEntity -> {
            List<RelativeResponse> relatives =
                    this.relativeRepository.findAllByCustomerIdAndSoftDeleteIsFalse(customerEntity.getId())
                            .stream().map(
                                    relativeEntity -> modelMapper.map(relativeEntity, RelativeResponse.class)
                            ).toList();
            CustomerResponse customerResponse = this.modelMapper.map(customerEntity, CustomerResponse.class);
            customerResponse.setRelativeEntities(relatives);
            return customerResponse;
        }).orElse(null);
    }


    @Override
    public WrapperResponse find(String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<CustomerEntity> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        CustomerResponse customerResponse = modelMapper.map(customerOptional.get(), CustomerResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), customerResponse, HttpStatus.OK
        );

    }

    private String generateCustomerCode() {
        long count = this.customerRepository.count();

        String customerCode = String.format("C%03d", count);

        do {
            if (this.customerRepository.existsByCustomerCode(customerCode)) {
                customerCode = String.format("C%03d", ++count);
                return customerCode;
            }
        } while (this.customerRepository.existsByCustomerCode(customerCode));

        return customerCode;
    }

    private List<RelativeResponse> createRelatives(CustomerAddRequest request, String customerId) {
        List<RelativeEntity> relativeEntities = request.getRelativeEntities().stream().map(
                relativeEntity -> {
                    RelativeEntity newRelativeEntity = new RelativeEntity();
                    newRelativeEntity.setRelativeName(relativeEntity.getRelativeName());
                    newRelativeEntity.setAge(relativeEntity.getAge());
                    newRelativeEntity.setJobName(relativeEntity.getJobName());
                    newRelativeEntity.setCustomerId(customerId);
                    newRelativeEntity.setCreatedAt(new Date());
                    return this.relativeRepository.save(newRelativeEntity);
                }
        ).toList();

        return relativeEntities.stream().map(
                relativeEntity -> modelMapper.map(relativeEntity, RelativeResponse.class)
        ).toList();
    }

    private List<RelativeResponse> updateRelatives(CustomerUpdateRequest request, String customerId) {

        List<RelativeEntity> relativeEntities = request.getRelativeEntities().stream().map(
                relativeEntity -> {
                    Optional<RelativeEntity> optionalRelative = this.relativeRepository
                            .findByCustomerIdAndRelativeNameAndSoftDeleteIsFalse(customerId, relativeEntity.getRelativeName());
                    RelativeEntity newRelativeEntity;
                    if (optionalRelative.isPresent()) {
                        newRelativeEntity = optionalRelative.get();
                        newRelativeEntity.setRelativeName(relativeEntity.getRelativeName());
                        newRelativeEntity.setAge(relativeEntity.getAge());
                        newRelativeEntity.setJobName(relativeEntity.getJobName());
                        newRelativeEntity.setUpdatedAt(new Date());
                    } else {
                        newRelativeEntity = new RelativeEntity();
                        newRelativeEntity.setRelativeName(relativeEntity.getRelativeName());
                        newRelativeEntity.setAge(relativeEntity.getAge());
                        newRelativeEntity.setJobName(relativeEntity.getJobName());
                        newRelativeEntity.setCustomerId(customerId);
                        newRelativeEntity.setCreatedAt(new Date());
                    }
                    return this.relativeRepository.save(newRelativeEntity);
                }
        ).toList();

        return relativeEntities.stream().map(
                relativeEntity -> modelMapper.map(relativeEntity, RelativeResponse.class)
        ).toList();

    }

    private void validateRelative(RelativeEntity relativeEntity) {
        if (relativeEntity == null) {
            throw new NullPointerException("Relative is null");
        }
        if (relativeEntity.getAge() == null) {
            throw new NullPointerException("Age is null");
        }
        if (relativeEntity.getJobName() == null || relativeEntity.getJobName().isEmpty()
                || relativeEntity.getJobName().isBlank()
        ) {
            throw new InvalidValueException("Invalid job name");
        }
    }

    private boolean isValidAddRequest(CustomerAddRequest request) {
        if (request != null) {

            if (request.getGender() == null) {
                return false;
            }

            if (request.getPhoneNumber() == null
                    || request.getPhoneNumber().isBlank() || request.getPhoneNumber().isEmpty()
                    || !isValidPhoneNumber(request.getPhoneNumber())
                    || this.customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                return false;
            }

            if (request.getEmail() == null
                    || request.getEmail().isBlank() || request.getEmail().isEmpty()
                    || !isValidEmail(request.getEmail())
                    || this.customerRepository.existsByEmail(request.getEmail())) {
                return false;
            }

            if (request.getProof() == null
                    || !isValidProof(request.getProof())
                    || this.customerRepository.existsCustomerByProof(request.getProof())) {
                return false;
            }

            if (request.getJobName() == null || request.getJobName().isBlank()
                    || request.getJobName().isEmpty()) {
                return false;
            }

            if (request.getDateOfBirth() == null) {
                return false;
            }

            for (AddressModel addressModel : request.getAddressModels()) {
                if (!isValidAddress(addressModel)) {
                    return false;
                }
            }

            request.getRelativeEntities().forEach(
                    this::validateRelative
            );
            return true;
        }
        return false;
    }

    private boolean isValidUpdateRequest(CustomerEntity customerEntity, CustomerUpdateRequest request) {
        if (request != null) {
            if (request.getGender() == null) {
                return false;
            }

            if (request.getPhoneNumber() == null
                    || request.getPhoneNumber().isBlank() || request.getPhoneNumber().isEmpty()
                    || !isValidPhoneNumber(request.getPhoneNumber())
                    || this.customerRepository.existsByPhoneNumberAndIdIsNot(
                    request.getPhoneNumber(), customerEntity.getId())) {
                return false;
            }

            if (request.getEmail() == null
                    || request.getEmail().isBlank() || request.getEmail().isEmpty()
                    || !isValidEmail(request.getEmail())
                    || this.customerRepository.existsByEmailAndIdIsNot(
                    request.getEmail(), customerEntity.getId())) {
                return false;
            }


            if (request.getProof() == null
                    || !isValidProof(request.getProof())
                    || this.customerRepository.existsCustomerByProofAndIdIsNot(
                    request.getProof(), customerEntity.getId())) {
                return false;
            }

            if (request.getJobName() == null || request.getJobName().isBlank()
                    || request.getJobName().isEmpty()) {
                return false;
            }

            if (request.getDateOfBirth() == null) {
                return false;
            }

            if (request.getStatusCustomer() == null ||
                    !customerEntity.getStatusCustomer().equals(StatusCustomer.POTENTIAL)
                            && request.getStatusCustomer().equals(StatusCustomer.POTENTIAL)) {
                return false;
            }

            for (AddressModel addressModel : request.getAddressModels()) {
                if (!isValidAddress(addressModel)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private boolean isValidProof(IdentityModel identityType) {
        switch (identityType.getTypeIdentity()) {
            case IDENTITY_CARD -> {
                return RegexConstant.REGEX_IDENTITY_CARD.matcher(identityType.getNumberIdentity()).matches();
            }
            case CITIZEN_IDENTITY_CARD -> {
                return RegexConstant.REGEX_CITIZEN_IDENTITY_CARD.matcher(identityType.getNumberIdentity()).matches();
            }
            case PASSPORT -> {
                return RegexConstant.REGEX_PASSPORT.matcher(identityType.getNumberIdentity()).matches();
            }
        }
        return false;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return RegexConstant.REGEX_PHONE_NUMBER.matcher(phoneNumber).matches();
    }

    private boolean isValidEmail(String email) {
        return RegexConstant.REGEX_EMAIL.matcher(email).matches();
    }

    private boolean isValidAddress(AddressModel addressModel) {
        if (addressModel == null || addressModel.getNational() == null
                || addressModel.getNational().isBlank() || addressModel.getNational().isEmpty()) {
            return false;
        }
        String VIETNAM_CODE = "VN";
        if (VIETNAM_CODE.equals(addressModel.getNational())) {
            if (addressModel.getHouseNumber() == null
                    || addressModel.getHouseNumber().isBlank() || addressModel.getHouseNumber().isEmpty()) {
                return false;
            }
            if (addressModel.getStreetName() == null
                    || addressModel.getStreetName().isBlank() || addressModel.getStreetName().isEmpty()) {
                return false;
            }
            if (addressModel.getWardName() == null
                    || addressModel.getWardName().isBlank() || addressModel.getWardName().isEmpty()) {
                return false;
            }
            if (addressModel.getDistrictName() == null
                    || addressModel.getDistrictName().isBlank() || addressModel.getDistrictName().isEmpty()) {
                return false;
            }
            if (addressModel.getCity() == null
                    || addressModel.getCity().isBlank() || addressModel.getCity().isEmpty()) {
                return false;
            }
            return true;
        }
        return true;
    }

    private void clearCustomerResponseCache(String customerId) {
        Cache customerResponseCache = cacheManager.getCache(CUSTOMER_VALUE_NAME);
        if (customerResponseCache != null) {
            customerResponseCache.evict(CUSTOMER_KEY_NAME + customerId);
        }
    }

}
