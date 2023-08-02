package com.lzx.myboot.mapper;

import com.lzx.myboot.entity.User;

import java.util.List;

/**
 * @author lzx
 * @date 2023/6/8 11:35
 * @description: TODO
 */
public interface UserDao {
    List<User> findAll();
    User findName(String name);
    boolean addUser(User user);
    boolean updateByName(String name,String age);
    boolean deleteByName(String name);
}
