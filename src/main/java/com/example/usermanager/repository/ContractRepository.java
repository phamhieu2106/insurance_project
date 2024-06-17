package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.ContractEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, String>,
        JpaSpecificationExecutor<ContractEntity> {

    boolean existsByContractCode(String contractCode);

    List<ContractEntity> findAllByCustomerIdAndSoftDeleteIsFalse(String customerId);

    Optional<ContractEntity> findByIdAndSoftDeleteIsFalse(String contractId);

    @Query(value = """
             UPDATE ContractEntity c
             SET c.statusContract =
             CASE\s
                 WHEN :now < c.contractStartDate THEN "NOT_EFFECT"
                 WHEN :now >= c.contractStartDate AND :now <= c.contractEndDate THEN "EFFECTED"
                 WHEN :now > c.contractEndDate THEN "END_EFFECTED"
             END
            \s""")
    @Modifying
    @Transactional
    void updateStatusContract(Date now);
}
