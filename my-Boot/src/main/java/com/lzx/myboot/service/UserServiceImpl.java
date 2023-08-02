package com.lzx.myboot.service;


import com.lzx.myboot.entity.User;
import com.lzx.myboot.mapper.UserMapper;
import com.lzx.myboot.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lzx
 * @date 2023/6/8 11:39
 * @description: TODO
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getAll() {
        return userMapper.selectAll();
    }

    @Override
    public User getDataByName(String name) {
        return userMapper.selectByName(name);
    }
}
