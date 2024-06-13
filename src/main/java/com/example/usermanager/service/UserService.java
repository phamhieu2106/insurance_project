package com.example.usermanager.service;

import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.user.UserResponse;
import org.springframework.http.ResponseEntity;

public interface UserService extends IService<UserRequest, UserUpdateRequest, UserResponse> {
    ResponseEntity<UserResponse> update(UserUpdateRequest request, String id);
}
