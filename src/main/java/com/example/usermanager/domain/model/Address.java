package com.example.usermanager.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {

    String houseNumber;

    String streetName;

    String wardName;

    String districtName;

    String city;

    String national;

}
