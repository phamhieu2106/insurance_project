package com.example.usermanager.domain.request.insurance;

import com.example.usermanager.domain.request.PageRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsurancePageRequest extends PageRequest {
}
