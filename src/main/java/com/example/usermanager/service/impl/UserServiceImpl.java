package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.User;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.user.UserResponse;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.AuthenticateService;
import com.example.usermanager.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuthenticateService authenticateService;


    @Autowired
    public UserServiceImpl(ModelMapper modelMapper
            , UserRepository userRepository, AuthenticateService authenticateService) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.authenticateService = authenticateService;
    }

    @Override
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> userResponses = userRepository.findAllBySoftDeleteIsFalse()
                .stream().map(user -> modelMapper.map(user, UserResponse.class)).toList();
        return ResponseEntity.ok(userResponses);
    }

    @Override
    public ResponseEntity<UserResponse> add(UserRequest request) {
        UserResponse userResponse = modelMapper.map(
                authenticateService.registerByAdmin(request), UserResponse.class);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<UserResponse> delete(String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        user.setSoftDelete(true);

        return ResponseEntity.ok(modelMapper.map(userRepository.save(user), UserResponse.class));
    }


    @Override
    //   admin Update
    public ResponseEntity<UserResponse> update(UserUpdateRequest request, String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        user.setRole(request.getRole());
        user.setUpdatedAt(new Date());

        return ResponseEntity.ok(modelMapper.map(userRepository.save(user), UserResponse.class));
    }

    @Override
    public ResponseEntity<UserResponse> find(String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        return ResponseEntity.ok(modelMapper.map(user, UserResponse.class));

    }
}
