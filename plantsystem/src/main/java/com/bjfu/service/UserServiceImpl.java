package com.bjfu.service;

import com.bjfu.mapper.UserMapper;
import com.bjfu.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;


    @Override
    public Integer register(User user) {
        Integer result = userMapper.count(user);
        if (result != 0) {
            return 0;
        } else {
            userMapper.insert(user);
            return 1;
        }
    }

    @Override
    public Integer login(User user) {
        Integer result = userMapper.login(user);
        return result != null ? result : 0;
    }
}
