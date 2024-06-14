package com.example.usermanager.service.impl;


import com.example.usermanager.domain.entity.Admin;
import com.example.usermanager.domain.entity.User;
import com.example.usermanager.domain.request.authenticate.LoginRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.enumeration.Role;
import com.example.usermanager.enumeration.UserRole;
import com.example.usermanager.exception.InvalidValueException;
import com.example.usermanager.repository.AdminRepository;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.AuthenticateService;
import com.example.usermanager.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticateServiceImpl implements AuthenticateService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticateServiceImpl(UserRepository userRepository, AdminRepository adminRepository
            , PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ResponseEntity<String> authenticate(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (loginRequest.getUsername().isBlank() || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> optionalUser = userRepository.findUserByUsernameAndSoftDeleteIsFalse(loginRequest.getUsername());
        if (optionalUser.isPresent()
                && passwordEncoder.matches(loginRequest.getPassword(), optionalUser.get().getPassword())) {

            return ResponseEntity.ok(getToken(optionalUser.get()));
        }

        Optional<Admin> optionalAdmin = adminRepository.findAdminByUsername(loginRequest.getUsername());
        if (optionalAdmin.isPresent()
                && passwordEncoder.matches(loginRequest.getPassword(), optionalAdmin.get().getPassword())) {

            return ResponseEntity.ok(getToken(optionalAdmin.get()));
        }


        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Override
    public ResponseEntity<String> registerByUser(UserRequest registerRequest) {

        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (registerRequest.getUsername().isBlank() || registerRequest.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())
                || adminRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = registerRequest.map(new User());
        user.setUserCode(generateUserCode(userRepository.count()));
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.NHAN_VIEN);
        user.setCreatedAt(new Date());
        user.setUserRole(UserRole.USER);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(getToken(user));
    }

    @Override
    public User registerByAdmin(UserRequest registerRequest) {

        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            throw new InvalidValueException("Invalid username or password");
        }
        if (registerRequest.getUsername().isBlank() || registerRequest.getPassword().isBlank()) {
            throw new InvalidValueException("Invalid username or password");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())
                || adminRepository.existsByUsername(registerRequest.getUsername())) {
            throw new InvalidValueException("Username already use");
        }

        User user = registerRequest.map(new User());
        user.setUserCode(generateUserCode(userRepository.count()));
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUserRole(UserRole.USER);
        user.setRole(Role.NHAN_VIEN);
        user.setCreatedAt(new Date());


        return userRepository.save(user);
    }

    private String getToken(UserDetails userDetails) {
        return jwtService.generateToken(userDetails);
    }

    private String generateUserCode(long number) {
        String userCode = String.format("U%03d", number);

        do {
            if (this.userRepository.existsByUserCode(userCode)) {
                userCode = String.format("C%03d", ++number);
                return userCode;
            }
        } while (this.userRepository.existsByUserCode(userCode));

        return userCode;
    }

//    Test

    @Override
    public String testAuth() {
        Admin admin = new Admin();
        admin.setUsername("phamhieu2106");
        admin.setPassword(passwordEncoder.encode("123"));
        admin.setAdminName("Pham Hieu");
        admin.setCreatedAt(new Date());
        admin.setCreatedBy("Test-System");
        admin.setUserRole(UserRole.ADMIN);
        adminRepository.save(admin);
        return "OK";
    }

}
