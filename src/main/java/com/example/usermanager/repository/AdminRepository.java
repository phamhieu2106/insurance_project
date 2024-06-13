package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {

    Optional<Admin> findAdminByUsername(String username);

    boolean existsByUsername(String username);
}
