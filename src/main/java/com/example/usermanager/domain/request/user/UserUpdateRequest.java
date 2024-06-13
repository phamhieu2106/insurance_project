package com.example.usermanager.domain.request.user;

import com.example.usermanager.enumeration.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    Role role;
}
