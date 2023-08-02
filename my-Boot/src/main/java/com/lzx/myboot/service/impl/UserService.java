package com.lzx.myboot.service.impl;


import com.lzx.myboot.entity.User;

import java.util.List;

/**
 * @author lzx
 * @date 2023/6/8 11:55
 * @description: TODO
 */
public interface UserService {
    // 获取all
    List<User> getAll();

    User getDataByName(String name);
}
