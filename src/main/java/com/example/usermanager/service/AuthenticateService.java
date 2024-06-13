package com.example.usermanager.service;

import com.example.usermanager.domain.entity.User;
import com.example.usermanager.domain.request.authenticate.LoginRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import org.springframework.http.ResponseEntity;

public interface AuthenticateService {

    ResponseEntity<String> authenticate(LoginRequest loginRequest);

    ResponseEntity<String> registerByUser(UserRequest registerRequest);

    User registerByAdmin(UserRequest registerRequest);

    String testAuth();
}
