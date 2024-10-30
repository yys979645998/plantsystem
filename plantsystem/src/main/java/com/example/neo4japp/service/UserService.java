package com.example.neo4japp.service;

import com.example.neo4japp.dto.User;


public interface UserService {


    Integer register(User user);

    Integer login(User user);
}
