package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.Customer;
import com.example.usermanager.domain.entity.Relative;
import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.domain.request.customer.CustomerAddRequest;
import com.example.usermanager.domain.request.customer.CustomerUpdateRequest;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.enumeration.StatusCustomer;
import com.example.usermanager.exception.InvalidValueException;
import com.example.usermanager.exception.ParseValueException;
import com.example.usermanager.repository.CustomerRepository;
import com.example.usermanager.repository.RelativeRepository;
import com.example.usermanager.service.CustomerService;
import com.example.usermanager.utils.contraint.RegexConstants;
import com.example.usermanager.utils.convert.DateConvert;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RelativeRepository relativeRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               RelativeRepository relativeRepository, ModelMapper modelMapper) {
        super();
        this.customerRepository = customerRepository;
        this.relativeRepository = relativeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(
                this.customerRepository.findAllBySoftDeleteIsFalse().stream()
                        .map(customer -> modelMapper.map(customer, CustomerResponse.class)
                        ).toList()
        );
    }

    @Override
    @Transactional(rollbackOn = InvalidValueException.class)
    public ResponseEntity<CustomerResponse> add(CustomerAddRequest request) {

        if (!validationAddCustomer(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }


        Customer customer = new Customer();
        try {
            customer.setDateOfBirth(DateConvert.convertDateToLocalDate(request.getDateOfBirth()));
        } catch (ParseException parseException) {
            throw new ParseValueException("Can't parse String to LocalDate");
        }
        customer.setCustomerCode(generateCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setGender(request.getGender());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setProof(request.getProof());
        customer.setJobName(request.getJobName());
        customer.setStatusCustomer(StatusCustomer.POTENTIAL);
        customer.setCreatedAt(LocalDateTime.now());

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customer), CustomerResponse.class);
        createRelatives(request, customerResponse.getId());

        return ResponseEntity.ok(customerResponse);
    }

    @Override
    public ResponseEntity<CustomerResponse> delete(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Customer customer = customerOptional.get();
        customer.setSoftDelete(true);

        this.customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    public ResponseEntity<CustomerResponse> update(CustomerUpdateRequest request, String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Customer customer = customerOptional.get();
        if (!validationUpdateCustomer(customer, request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            customer.setDateOfBirth(DateConvert.convertDateToLocalDate(request.getDateOfBirth()));
        } catch (ParseException parseException) {
            throw new ParseValueException("Can't parse String to LocalDate");
        }
        customer.setCustomerCode(generateCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setGender(request.getGender());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setProof(request.getProof());
        customer.setJobName(request.getJobName());
        customer.setStatusCustomer(request.getStatusCustomer());
        customer.setUpdatedAt(LocalDateTime.now());

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customer), CustomerResponse.class);


        return ResponseEntity.ok(customerResponse);
    }

    @Override
    @Cacheable(value = "customerResponse", key = "#id")
    public ResponseEntity<CustomerResponse> find(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        return customerOptional.map(customer -> ResponseEntity.ok(modelMapper.map(customer, CustomerResponse.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

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

    private void createRelatives(CustomerAddRequest request, String customerId) {
        request.getRelatives().forEach(
                relative -> {
                    if (validationAddRelative(relative, customerId)) {
                        Relative newRelative = new Relative();
                        newRelative.setRelativeName(relative.getRelativeName());
                        newRelative.setAge(relative.getAge());
                        newRelative.setJobName(relative.getJobName());
                        newRelative.setCustomerId(customerId);
                        newRelative.setCreatedAt(LocalDateTime.now());
                        this.relativeRepository.save(newRelative);
                    }
                }
        );
    }

    private boolean validationAddRelative(Relative relative, String customerId) {
        if (relative == null) {
            throw new NullPointerException("Relative is null");
        }
        if (relative.getAge() == null) {
            throw new NullPointerException("Age is null");
        }
        if (relative.getJobName() == null || relative.getJobName().isEmpty()
                || relative.getJobName().isBlank()
        ) {
            throw new InvalidValueException("Invalid job name");
        }
        if (customerId == null || customerId.isEmpty()
                || customerId.isBlank()) {
            throw new InvalidValueException("Invalid customerId");
        }
        return true;
    }

    private boolean validationAddCustomer(CustomerAddRequest request) {
        if (request != null) {

            if (request.getGender() == null) {
                return false;
            }

            if (request.getPhoneNumber() == null
                    || request.getPhoneNumber().isBlank() || request.getPhoneNumber().isEmpty()
                    || !isPhoneNumberValid(request.getPhoneNumber())
                    || this.customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                return false;
            }

            if (request.getEmail() == null
                    || request.getEmail().isBlank() || request.getEmail().isEmpty()
                    || !isEmailValid(request.getEmail())
                    || this.customerRepository.existsByEmail(request.getEmail())) {
                return false;
            }

            if (request.getProof() == null
                    || !isProofValid(request.getProof())
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

            return isAddressValid(request.getAddress());
        }
        return false;
    }

    private boolean validationUpdateCustomer(Customer customer, CustomerUpdateRequest request) {
        if (request != null) {
            if (request.getGender() == null) {
                return false;
            }

            if (request.getPhoneNumber() == null
                    || request.getPhoneNumber().isBlank() || request.getPhoneNumber().isEmpty()
                    || !isPhoneNumberValid(request.getPhoneNumber())
                    || this.customerRepository.existsByPhoneNumberAndIdIsNot(
                    request.getPhoneNumber(), customer.getId())) {
                return false;
            }

            if (request.getEmail() == null
                    || request.getEmail().isBlank() || request.getEmail().isEmpty()
                    || !isEmailValid(request.getEmail())
                    || this.customerRepository.existsByEmailAndIdIsNot(
                    request.getEmail(), customer.getId())) {
                return false;
            }


            if (request.getProof() == null
                    || !isProofValid(request.getProof())
                    || this.customerRepository.existsCustomerByProofAndIdIsNot(
                    request.getProof(), customer.getId())) {
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
                    !customer.getStatusCustomer().equals(StatusCustomer.POTENTIAL)
                            && request.getStatusCustomer().equals(StatusCustomer.POTENTIAL)) {
                return false;
            }

            return isAddressValid(request.getAddress());
        }
        return false;
    }

    private boolean isProofValid(IdentityType identityType) {
        switch (identityType.getTypeIdentity()) {
            case IDENTITY_CARD -> {
                return RegexConstants.REGEX_IDENTITY_CARD.matcher(identityType.getNumberIdentity()).matches();
            }
            case CITIZEN_IDENTITY_CARD -> {
                return RegexConstants.REGEX_CITIZEN_IDENTITY_CARD.matcher(identityType.getNumberIdentity()).matches();
            }
            case PASSPORT -> {
                return RegexConstants.REGEX_PASSPORT.matcher(identityType.getNumberIdentity()).matches();
            }
            default -> {
                return false;
            }
        }
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return RegexConstants.REGEX_PHONE_NUMBER.matcher(phoneNumber).matches();
    }

    private boolean isEmailValid(String email) {
        return RegexConstants.REGEX_EMAIL.matcher(email).matches();
    }

    private boolean isAddressValid(Address address) {
        if (address == null || address.getNational() == null
                || address.getNational().isBlank() || address.getNational().isEmpty()) {
            return false;
        }
        if ("VN".equals(address.getNational())) {
            if (address.getHouseNumber() == null
                    || address.getHouseNumber().isBlank() || address.getHouseNumber().isEmpty()) {
                return false;
            }
            if (address.getStreetName() == null
                    || address.getStreetName().isBlank() || address.getStreetName().isEmpty()) {
                return false;
            }
            if (address.getWardName() == null
                    || address.getWardName().isBlank() || address.getWardName().isEmpty()) {
                return false;
            }
            if (address.getDistrictName() == null
                    || address.getDistrictName().isBlank() || address.getDistrictName().isEmpty()) {
                return false;
            }
            if (address.getCity() == null
                    || address.getCity().isBlank() || address.getCity().isEmpty()) {
                return false;
            }
            return true;
        }
        return true;
    }

}
