package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.InsuranceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<InsuranceEntity, String>,
        JpaSpecificationExecutor<InsuranceEntity> {

    boolean existsByInsuranceCode(String code);

    Optional<InsuranceEntity> findByIdAndSoftDeleteIsFalse(String id);

}
