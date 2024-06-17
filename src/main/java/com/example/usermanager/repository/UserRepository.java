package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    Page<UserEntity> findAllBySoftDeleteIsFalse(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByUserCode(String userCode);

    Optional<UserEntity> findUserByIdAndSoftDeleteIsFalse(String id);

    Optional<UserEntity> findUserByUsernameAndSoftDeleteIsFalse(String username);

    long count();
}
