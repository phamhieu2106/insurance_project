package com.example.usermanager.service;

import com.example.usermanager.domain.request.user.UserPageRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;

public interface UserService extends IService<UserRequest, UserUpdateRequest> {
    WrapperResponse update(UserUpdateRequest request, String id);

    WrapperResponse findAll(UserPageRequest request);
}
