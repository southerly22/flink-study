package com.lzx.myboot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzx.myboot.entity.User;
import com.lzx.myboot.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzx
 * @date 2023/6/8 13:29
 * @description: TODO
 */
@RestController
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @RequestMapping("/all")
    public String getAll(){
        List<User> userList = userService.getAll();
        ArrayList<String> arrayList = new ArrayList<>();
        for (User user : userList) {
            arrayList.add(JSON.toJSONString(user));
        }
        return arrayList.toString();
    }

    @RequestMapping("/get")
    public String getDataByName(@RequestParam(value = "name",defaultValue = "马艳艳") String name){
        User user = userService.getDataByName(name);
        return JSON.toJSONString(user);
    }
}
