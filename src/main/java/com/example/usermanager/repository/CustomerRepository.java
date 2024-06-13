package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Customer;
import com.example.usermanager.domain.model.IdentityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    List<Customer> findAllBySoftDeleteIsFalse();

    Optional<Customer> findCustomerByIdAndSoftDeleteIsFalse(String id);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsCustomerByProof(IdentityType proof);

    boolean existsByEmailAndIdIsNot(String email, String Id);

    boolean existsByPhoneNumberAndIdIsNot(String phoneNumber, String id);

    boolean existsCustomerByProofAndIdIsNot(IdentityType proof, String id);

    long count();
}
