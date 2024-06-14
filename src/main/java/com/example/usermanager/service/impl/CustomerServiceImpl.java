package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.Customer;
import com.example.usermanager.domain.entity.Relative;
import com.example.usermanager.domain.model.Address;
import com.example.usermanager.domain.model.IdentityType;
import com.example.usermanager.domain.request.customer.CustomerAddRequest;
import com.example.usermanager.domain.request.customer.CustomerUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.customer.CustomerResponse;
import com.example.usermanager.domain.response.relative.RelativeResponse;
import com.example.usermanager.enumeration.StatusCustomer;
import com.example.usermanager.exception.InvalidValueException;
import com.example.usermanager.repository.CustomerRepository;
import com.example.usermanager.repository.RelativeRepository;
import com.example.usermanager.service.CustomerService;
import com.example.usermanager.utils.contraint.RegexConstants;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RelativeRepository relativeRepository;
    private final ModelMapper modelMapper;
    private final CacheManager cacheManager;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               RelativeRepository relativeRepository
            , ModelMapper modelMapper, CacheManager cacheManager) {
        super();
        this.customerRepository = customerRepository;
        this.relativeRepository = relativeRepository;
        this.modelMapper = modelMapper;
        this.cacheManager = cacheManager;
    }

    @Override
    public WrapperResponse findAll() {
        List<CustomerResponse> list = this.customerRepository.findAllBySoftDeleteIsFalse().stream()
                .map(customer -> {
                    List<RelativeResponse> relatives =
                            this.relativeRepository.findAllByCustomerIdAndSoftDeleteIsFalse(customer.getId())
                                    .stream().map(
                                            relative -> modelMapper.map(relative, RelativeResponse.class)
                                    ).toList();
                    CustomerResponse customerResponse = this.modelMapper.map(customer, CustomerResponse.class);
                    customerResponse.setRelatives(relatives);
                    return customerResponse;
                }).toList();

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), list, HttpStatus.OK
        );
    }

    @Override
    @Transactional(rollbackOn = InvalidValueException.class)
    public WrapperResponse add(CustomerAddRequest request) {

        if (!validationAddCustomer(request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Customer customer = new Customer();
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerCode(generateCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setGender(request.getGender());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddresses(request.getAddresses());
        customer.setProof(request.getProof());
        customer.setJobName(request.getJobName());
        customer.setStatusCustomer(StatusCustomer.POTENTIAL);
        customer.setCreatedAt(new Date());

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customer), CustomerResponse.class);

        List<RelativeResponse> relativeResponses = createRelatives(request, customerResponse.getId());
        customerResponse.setRelatives(relativeResponses);

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

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Customer customer = customerOptional.get();
        customer.setSoftDelete(true);

        this.customerRepository.save(customer);

        return WrapperResponse.returnResponse(
                true, HttpStatus.NO_CONTENT.getReasonPhrase(), null, HttpStatus.NO_CONTENT
        );
    }

    @Override
    @Transactional
    public WrapperResponse update(CustomerUpdateRequest request, String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

        if (customerOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        Customer customer = customerOptional.get();
        if (!validationUpdateCustomer(customer, request)) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerCode(customer.getCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setGender(request.getGender());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddresses(request.getAddresses());
        customer.setProof(request.getProof());
        customer.setJobName(request.getJobName());
        customer.setStatusCustomer(request.getStatusCustomer());
        customer.setUpdatedAt(new Date());

        // Lấy ID của khách hàng
        clearCustomerResponseCache(customer.getId());

        //tạo key mới
        String newCacheKey = "customer_" + customer.getId();
        Objects.requireNonNull(cacheManager
                        .getCache("customerResponse"))
                .put(newCacheKey, modelMapper.map(customer, CustomerResponse.class));

        CustomerResponse customerResponse = modelMapper.map(customerRepository.save(customer), CustomerResponse.class);

        List<RelativeResponse> relativeResponses = updateRelatives(request, customerResponse.getId());
        customerResponse.setRelatives(relativeResponses);

        return WrapperResponse.returnResponse(
                false, HttpStatus.OK.getReasonPhrase(), customerResponse, HttpStatus.OK
        );
    }

    @Cacheable(value = "customerResponse", key = "'customer_' + #id")
    public CustomerResponse findCustomerById(String id) {

        if (id == null || id.isEmpty() || id.isBlank()) {
            return null;
        }

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);
        return customerOptional.map(customer -> {
            List<RelativeResponse> relatives =
                    this.relativeRepository.findAllByCustomerIdAndSoftDeleteIsFalse(customer.getId())
                            .stream().map(
                                    relative -> modelMapper.map(relative, RelativeResponse.class)
                            ).toList();
            CustomerResponse customerResponse = this.modelMapper.map(customer, CustomerResponse.class);
            customerResponse.setRelatives(relatives);
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

        Optional<Customer> customerOptional = customerRepository.findCustomerByIdAndSoftDeleteIsFalse(id);

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
        List<Relative> relatives = request.getRelatives().stream().map(
                relative -> {
                    if (validationAddRelative(relative, customerId)) {
                        Relative newRelative = new Relative();
                        newRelative.setRelativeName(relative.getRelativeName());
                        newRelative.setAge(relative.getAge());
                        newRelative.setJobName(relative.getJobName());
                        newRelative.setCustomerId(customerId);
                        newRelative.setCreatedAt(new Date());
                        return this.relativeRepository.save(newRelative);
                    }
                    return null;
                }
        ).toList();

        return relatives.stream().map(
                relative -> modelMapper.map(relative, RelativeResponse.class)
        ).toList();
    }

    private List<RelativeResponse> updateRelatives(CustomerUpdateRequest request, String customerId) {

        List<Relative> relatives = request.getRelatives().stream().map(
                relative -> {
                    Optional<Relative> optionalRelative = this.relativeRepository
                            .findByCustomerIdAndRelativeNameAndSoftDeleteIsFalse(customerId, relative.getRelativeName());
                    Relative newRelative;
                    if (optionalRelative.isPresent()) {
                        newRelative = optionalRelative.get();
                        newRelative.setRelativeName(relative.getRelativeName());
                        newRelative.setAge(relative.getAge());
                        newRelative.setJobName(relative.getJobName());
                        newRelative.setUpdatedAt(new Date());
                    } else {
                        newRelative = new Relative();
                        newRelative.setRelativeName(relative.getRelativeName());
                        newRelative.setAge(relative.getAge());
                        newRelative.setJobName(relative.getJobName());
                        newRelative.setCustomerId(customerId);
                        newRelative.setCreatedAt(new Date());
                    }
                    return this.relativeRepository.save(newRelative);
                }
        ).toList();

        return relatives.stream().map(
                relative -> modelMapper.map(relative, RelativeResponse.class)
        ).toList();

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

            for (Address address : request.getAddresses()) {
                if (!isValidAddress(address)) {
                    return false;
                }
            }
            return true;
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
                    || !isValidPhoneNumber(request.getPhoneNumber())
                    || this.customerRepository.existsByPhoneNumberAndIdIsNot(
                    request.getPhoneNumber(), customer.getId())) {
                return false;
            }

            if (request.getEmail() == null
                    || request.getEmail().isBlank() || request.getEmail().isEmpty()
                    || !isValidEmail(request.getEmail())
                    || this.customerRepository.existsByEmailAndIdIsNot(
                    request.getEmail(), customer.getId())) {
                return false;
            }


            if (request.getProof() == null
                    || !isValidProof(request.getProof())
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

            for (Address address : request.getAddresses()) {
                if (!isValidAddress(address)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private boolean isValidProof(IdentityType identityType) {
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

    private boolean isValidPhoneNumber(String phoneNumber) {
        return RegexConstants.REGEX_PHONE_NUMBER.matcher(phoneNumber).matches();
    }

    private boolean isValidEmail(String email) {
        return RegexConstants.REGEX_EMAIL.matcher(email).matches();
    }

    private boolean isValidAddress(Address address) {
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

    private void clearCustomerResponseCache(String customerId) {
        Cache customerResponseCache = cacheManager.getCache("customerResponse");
        if (customerResponseCache != null) {
            customerResponseCache.evict("customer_" + customerId);
        }
    }
}
