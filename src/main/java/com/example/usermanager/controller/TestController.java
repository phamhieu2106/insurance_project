package com.example.usermanager.controller;

import com.example.usermanager.service.AuthenticateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/v1/admin/test")
public class TestController {

    private final AuthenticateService authenticateService;

    @Autowired
    public TestController(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(authenticateService.testAuth());
    }

}
