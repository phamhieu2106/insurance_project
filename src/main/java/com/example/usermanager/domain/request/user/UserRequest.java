package com.example.usermanager.domain.request.user;

import com.example.usermanager.domain.entity.UserEntity;
import com.example.usermanager.enumeration.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {

    @NotBlank
    String username;
    @NotBlank
    String password;

    Role role;

    public UserEntity map(UserEntity userEntity) {
        userEntity.setUsername(this.username);
        userEntity.setPassword(this.password);
        userEntity.setRole(this.role);
        return userEntity;
    }
}
