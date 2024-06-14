package com.example.usermanager.service;

import com.example.usermanager.domain.request.authenticate.LoginRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.response.WrapperResponse;

public interface AuthenticateService {

    WrapperResponse authenticate(LoginRequest loginRequest);

    WrapperResponse registerByUser(UserRequest registerRequest);

    WrapperResponse registerByAdmin(UserRequest registerRequest);

    WrapperResponse testAuth();

}
