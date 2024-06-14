package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Relative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelativeRepository extends JpaRepository<Relative, String> {

    List<Relative> findAllByCustomerIdAndSoftDeleteIsFalse(String customerId);

    Optional<Relative> findByCustomerIdAndRelativeNameAndSoftDeleteIsFalse(String customerId, String relativeName);
}
