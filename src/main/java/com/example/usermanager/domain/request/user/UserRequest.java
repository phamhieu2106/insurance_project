package com.example.usermanager.domain.request.user;

import com.example.usermanager.domain.entity.User;
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

    public User map(User user) {
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setRole(this.role);
        return user;
    }
}
