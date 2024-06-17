package com.example.usermanager.controller;

import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.service.UserService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.UserSpecifications;
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
    public WrapperResponse findAll(
            @RequestParam(name = "pageNumber",
                    required = false, defaultValue = PageConstant.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "pageSize",
                    required = false, defaultValue = PageConstant.PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy",
                    required = false, defaultValue = UserSpecifications.USER_CODE) String sortBy,
            @RequestParam(name = "sortType",
                    required = false, defaultValue = PageConstant.PAGE_SORT_TYPE) String sortType,
            @RequestParam(name = "keyword",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String keyword,
            @RequestParam(name = "role",
                    required = false, defaultValue = PageConstant.PAGE_DEFAULT_VALUE) String role
    ) {
        return this.userService.findAll(pageNumber, pageSize, sortBy, sortType, keyword, role);
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
