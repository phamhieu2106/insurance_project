package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.AdminEntity;
import com.example.usermanager.domain.entity.UserEntity;
import com.example.usermanager.repository.AdminRepository;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {

        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username provided");
        }

        Optional<UserEntity> userOptional = userRepository.findUserByUsernameAndSoftDeleteIsFalse(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        Optional<AdminEntity> adminOptional = adminRepository.findAdminByUsername(username);
        if (adminOptional.isPresent()) {
            return adminOptional.get();
        }


        throw new UsernameNotFoundException("Not found user with username: " + username);
    }
}
