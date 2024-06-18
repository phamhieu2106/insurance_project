package com.example.usermanager.controller;

import com.example.usermanager.domain.request.user.UserPageRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public WrapperResponse findAll(@RequestBody UserPageRequest request) {
        return this.userService.findAll(request);
    }

    @GetMapping("/{id}")
    public WrapperResponse findById(@PathVariable String id) {
        return this.userService.find(id);
    }

    @PostMapping
    public WrapperResponse addUser(@Valid @RequestBody UserRequest userRequest) {
        return this.userService.add(userRequest);
    }

    @PutMapping("/{id}")
    public WrapperResponse updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateRequest userRequest) {
        return this.userService.update(userRequest, id);
    }

    @DeleteMapping("/{id}")
    public WrapperResponse deleteUser(@PathVariable String id) {
        return this.userService.delete(id);
    }

}
