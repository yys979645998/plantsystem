package com.bjfu.service;

import com.bjfu.pojo.User;


public interface UserService {


    Integer register(User user);

    Integer login(User user);
}
