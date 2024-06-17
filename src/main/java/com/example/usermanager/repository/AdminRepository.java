package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, String> {

    Optional<AdminEntity> findAdminByUsername(String username);

    boolean existsByUsername(String username);
}
