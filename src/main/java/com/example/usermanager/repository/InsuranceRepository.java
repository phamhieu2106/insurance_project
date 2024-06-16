package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, String> {

    boolean existsByInsuranceCode(String code);

    Optional<Insurance> findByIdAndSoftDeleteIsFalse(String id);

    List<Insurance> findAllBySoftDeleteIsFalse();
}
