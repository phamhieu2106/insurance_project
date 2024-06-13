package com.example.usermanager.repository;

import com.example.usermanager.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


    boolean existsByUsername(String username);

    boolean existsByUserCode(String userCode);

    List<User> findAllBySoftDeleteIsFalse();

    Optional<User> findUserByIdAndSoftDeleteIsFalse(String id);

    Optional<User> findUserByUsernameAndSoftDeleteIsFalse(String username);

    long count();
}
