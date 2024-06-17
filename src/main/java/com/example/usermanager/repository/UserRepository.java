package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByUsername(String username);

    boolean existsByUserCode(String userCode);

    List<UserEntity> findAllBySoftDeleteIsFalse();

    Optional<UserEntity> findUserByIdAndSoftDeleteIsFalse(String id);

    Optional<UserEntity> findUserByUsernameAndSoftDeleteIsFalse(String username);

    long count();
}
