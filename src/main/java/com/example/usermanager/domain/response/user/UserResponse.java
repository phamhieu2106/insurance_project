package com.example.usermanager.domain.response.user;

import com.example.usermanager.enumeration.Role;
import com.example.usermanager.enumeration.UserRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String userCode;
    Role role;
    UserRole userRole;
}
