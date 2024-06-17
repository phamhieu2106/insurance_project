package com.example.usermanager.config;

import com.example.usermanager.domain.entity.AdminEntity;
import com.example.usermanager.domain.entity.UserEntity;
import com.example.usermanager.repository.AdminRepository;
import com.example.usermanager.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class ApplicationConfig {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public ApplicationConfig(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetail -> {
            Optional<UserEntity> userOptional = userRepository.findUserByUsernameAndSoftDeleteIsFalse(userDetail);
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                Optional<AdminEntity> adminOptional = adminRepository.findAdminByUsername(userDetail);
                if (adminOptional.isPresent()) {
                    return adminOptional.get();
                } else {
                    throw new UsernameNotFoundException("Not found username Admin or User");
                }
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
