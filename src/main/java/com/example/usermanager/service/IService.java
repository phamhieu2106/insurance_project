package com.example.usermanager.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface IService<Q, U, P> {

    ResponseEntity<List<P>> findAll();

    ResponseEntity<P> add(Q request);

    ResponseEntity<P> delete(String id);

    ResponseEntity<P> update(U request, String id);

    ResponseEntity<P> find(String id);

}
