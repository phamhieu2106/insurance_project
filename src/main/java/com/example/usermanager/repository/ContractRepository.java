package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Contract;
import com.example.usermanager.enumeration.StatusContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    boolean existsByContractCode(String contractCode);

    boolean existsByCustomerIdAndStatusContractNot(String customerId, StatusContract statusContract);

    List<Contract> findAllBySoftDeleteIsFalse();

    Optional<Contract> findByIdAndSoftDeleteIsFalse(String contractId);
}
