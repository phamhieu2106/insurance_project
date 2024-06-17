package com.example.usermanager.service;

import com.example.usermanager.domain.response.WrapperResponse;
import org.springframework.stereotype.Component;

@Component
public interface IService<Q, U> {

    WrapperResponse add(Q request);

    WrapperResponse delete(String id);

    WrapperResponse update(U request, String id);

    WrapperResponse find(String id);

}
