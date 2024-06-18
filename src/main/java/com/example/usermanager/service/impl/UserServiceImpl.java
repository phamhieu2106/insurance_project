package com.example.usermanager.service.impl;

import com.example.usermanager.domain.entity.UserEntity;
import com.example.usermanager.domain.request.user.UserPageRequest;
import com.example.usermanager.domain.request.user.UserRequest;
import com.example.usermanager.domain.request.user.UserUpdateRequest;
import com.example.usermanager.domain.response.WrapperResponse;
import com.example.usermanager.domain.response.user.UserResponse;
import com.example.usermanager.repository.UserRepository;
import com.example.usermanager.service.AuthenticateService;
import com.example.usermanager.service.UserService;
import com.example.usermanager.utils.contraint.PageConstant;
import com.example.usermanager.utils.specific.UserSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuthenticateService authenticateService;


    @Override
    public WrapperResponse findAll(UserPageRequest request) {

        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize()
                , PageConstant.getSortBy(request.getSortBys(), request.getSortOrder()));
        Specification<UserEntity> specification = UserSpecifications.withKeywordAndRole(
                request.getKeyword(), request.getRole());
        Page<UserEntity> usersPage = userRepository.findAll(specification, pageable);

        List<UserResponse> userResponses = usersPage.getContent()
                .stream().map(
                        user -> modelMapper.map(user, UserResponse.class)
                ).toList();

        Page<UserResponse> responsePage = new PageImpl<>(userResponses, pageable, usersPage.getTotalElements());

        return WrapperResponse.returnResponse(
                true, HttpStatus.OK.getReasonPhrase(),
                responsePage, HttpStatus.OK
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

        Optional<UserEntity> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        UserEntity userEntity = userOptional.get();
        userEntity.setSoftDelete(true);

        userRepository.save(userEntity);

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

        Optional<UserEntity> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        UserEntity userEntity = userOptional.get();
        userEntity.setRole(request.getRole());
        userEntity.setUpdatedAt(new Date());

        UserResponse userResponse = modelMapper.map(userRepository.save(userEntity), UserResponse.class);

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

        Optional<UserEntity> userOptional = userRepository.findUserByIdAndSoftDeleteIsFalse(id);
        if (userOptional.isEmpty()) {
            return WrapperResponse.returnResponse(
                    false, HttpStatus.NOT_FOUND.getReasonPhrase(), null, HttpStatus.NOT_FOUND
            );
        }

        UserEntity userEntity = userOptional.get();
        UserResponse userResponse = modelMapper.map(userEntity, UserResponse.class);

        return WrapperResponse.returnResponse(
                true, HttpStatus.FOUND.getReasonPhrase(), userResponse, HttpStatus.FOUND
        );
    }
}
