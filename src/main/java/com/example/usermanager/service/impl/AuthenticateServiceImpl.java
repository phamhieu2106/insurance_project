package com.example.usermanager.service.impl;


import com.example.usermanager.domain.entity.AdminEntity;
import com.example.usermanager.domain.entity.UserEntity;
import com.example.usermanager.domain.request.authenticate.LoginRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.user.UserResponse;
import com.example.usermanager.enumeration.Role;
import com.example.usermanager.enumeration.UserRole;
import com.example.usermanager.repository.AdminRepository;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.AuthenticateService;
import com.example.usermanager.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    @Override
    public WrapperResponse authenticate(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (loginRequest.getUsername().isBlank() || loginRequest.getPassword().isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<UserEntity> optionalUser = userRepository
                .findUserByUsernameAndSoftDeleteIsFalse(loginRequest.getUsername());

        if (optionalUser.isPresent()
                && passwordEncoder.matches(loginRequest.getPassword(), optionalUser.get().getPassword())) {

            return WrapperResponse.returnResponse(
                    true, HttpStatus.OK.getReasonPhrase(), getToken(optionalUser.get()), HttpStatus.OK
            );
        }

        Optional<AdminEntity> optionalAdmin = adminRepository.findAdminByUsername(loginRequest.getUsername());
        if (optionalAdmin.isPresent()
                && passwordEncoder.matches(loginRequest.getPassword(), optionalAdmin.get().getPassword())) {

            return WrapperResponse.returnResponse(
                    true, HttpStatus.OK.getReasonPhrase(), getToken(optionalAdmin.get()), HttpStatus.OK
            );
        }

        return WrapperResponse.returnResponse(
                false, HttpStatus.UNAUTHORIZED.getReasonPhrase(), null, HttpStatus.UNAUTHORIZED
        );
    }

    @Override
    public WrapperResponse registerByUser(UserRequest registerRequest) {

        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (registerRequest.getUsername().isBlank() || registerRequest.getPassword().isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())
                || adminRepository.existsByUsername(registerRequest.getUsername())) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.CONFLICT.getReasonPhrase(), null, HttpStatus.CONFLICT
            );
        }

        UserEntity userEntity = registerRequest.map(new UserEntity());
        userEntity.setUserCode(generateUserCode(userRepository.count()));
        userEntity.setUsername(registerRequest.getUsername());
        userEntity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userEntity.setRole(Role.NHAN_VIEN);
        userEntity.setCreatedAt(new Date());
        userEntity.setUserRole(UserRole.USER);


        UserResponse userResponse = modelMapper.map(userRepository.save(userEntity), UserResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.CREATED.getReasonPhrase(), userResponse, HttpStatus.CREATED
        );
    }

    @Override
    public WrapperResponse registerByAdmin(UserRequest registerRequest) {

        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (registerRequest.getUsername().isBlank() || registerRequest.getPassword().isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())
                || adminRepository.existsByUsername(registerRequest.getUsername())) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.CONFLICT.getReasonPhrase(), null, HttpStatus.CONFLICT
            );
        }

        UserEntity userEntity = registerRequest.map(new UserEntity());
        userEntity.setUserCode(generateUserCode(userRepository.count()));
        userEntity.setUsername(registerRequest.getUsername());
        userEntity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userEntity.setUserRole(UserRole.USER);
        userEntity.setRole(Role.NHAN_VIEN);
        userEntity.setCreatedAt(new Date());

        UserResponse userResponse = modelMapper.map(userRepository.save(userEntity), UserResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), userResponse, HttpStatus.OK
        );
    }

    //    Test

    @Override
    public WrapperResponse testAuth() {
        AdminEntity admin = new AdminEntity();
        admin.setUsername("phamhieu2106");
        admin.setPassword(passwordEncoder.encode("123"));
        admin.setAdminName("Pham Hieu");
        admin.setCreatedAt(new Date());
        admin.setCreatedBy("Test-System");
        admin.setUserRole(UserRole.ADMIN);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), adminRepository.save(admin), HttpStatus.OK
        );
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

}
