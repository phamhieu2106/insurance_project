package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.InsuranceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<InsuranceEntity, String> {

    boolean existsByInsuranceCode(String code);

    Optional<InsuranceEntity> findByIdAndSoftDeleteIsFalse(String id);

    List<InsuranceEntity> findAllBySoftDeleteIsFalse();
}
