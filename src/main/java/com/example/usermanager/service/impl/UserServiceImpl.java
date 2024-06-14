package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.User;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.user.UserResponse;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.AuthenticateService;
import com.example.usermanager.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public WrapperResponse findAll() {
        List<UserResponse> userResponses = userRepository.findAllBySoftDeleteIsFalse()
                .stream().map(user -> modelMapper.map(user, UserResponse.class)).toList();

        if (userResponses.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), userResponses, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse add(UserRequest request) {
        UserResponse userResponse = modelMapper.map(
                authenticateService.registerByAdmin(request).getData(), UserResponse.class);

        if (userResponse == null) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }
        return WrapperResponse.returnResponse(
                true, HttpStatus.CREATED.getReasonPhrase(), userResponse, HttpStatus.CREATED
        );
    }

    @Override
    public WrapperResponse delete(String id) {
        if (id == null || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        User user = userOptional.get();
        user.setSoftDelete(true);

        userRepository.save(user);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), null, HttpStatus.OK
        );
    }


    @Override
    //   admin Update
    public WrapperResponse update(UserUpdateRequest request, String id) {
        if (id == null || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        User user = userOptional.get();
        user.setRole(request.getRole());
        user.setUpdatedAt(new Date());

        UserResponse userResponse = modelMapper.map(userRepository.save(user), UserResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(), userResponse, HttpStatus.OK
        );
    }

    @Override
    public WrapperResponse find(String id) {
        if (id == null || id.isBlank()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, HttpStatus.BAD_REQUEST
            );
        }

        Optional<User> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        User user = userOptional.get();
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.FOUND.getReasonPhrase(), userResponse, HttpStatus.FOUND
        );
    }
}
