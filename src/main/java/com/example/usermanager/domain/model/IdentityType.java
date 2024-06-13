package com.example.usermanager.domain.model;

import com.example.usermanager.enumeration.Proof;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdentityType {

    @Enumerated(EnumType.STRING)
    Proof typeIdentity;
    String numberIdentity;

}
