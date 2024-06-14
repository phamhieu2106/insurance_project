package com.example.usermanager.controller;

import com.example.usermanager.domain.request.authenticate.LoginRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.AuthenticateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/v1")
public class AuthenticateController {

    private final AuthenticateService authenticateService;

    @Autowired
    public AuthenticateController(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @PostMapping("/authenticate")
    public WrapperResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return this.authenticateService.authenticate(loginRequest);
    }

    @PostMapping("/register")
    public WrapperResponse register(@Valid @RequestBody UserRequest userRequest) {
        return this.authenticateService.registerByUser(userRequest);
    }

}
