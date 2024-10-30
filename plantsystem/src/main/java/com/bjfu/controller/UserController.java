package com.bjfu.controller;

import com.bjfu.service.UserService;
import com.bjfu.pojo.Result;
import com.bjfu.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    //注册用户
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        int count = userService.register(user);
        if (count == 0) {
            return Result.error("该账号已被注册");
        } else {
            return Result.success("注册成功");
        }

    }

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        int count = userService.login(user);
        if (count == 1) {
            return Result.success();
        } else {
            return Result.error();
        }
    }
}
