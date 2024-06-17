package com.example.usermanager.domain.request.authenticate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotNull
    @NotBlank
    String username;

    @NotNull
    @NotBlank
    String password;
}
