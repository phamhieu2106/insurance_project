package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.Relative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelativeRepository extends JpaRepository<Relative, String> {
}
