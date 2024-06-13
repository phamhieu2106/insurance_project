package com.example.usermanager.controller;

import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.user.UserResponse;
import com.example.usermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return this.userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        return this.userService.find(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserRequest userRequest) {
        return this.userService.add(userRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateRequest userRequest) {
        return this.userService.update(userRequest, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String id) {
        return this.userService.delete(id);
    }
}
