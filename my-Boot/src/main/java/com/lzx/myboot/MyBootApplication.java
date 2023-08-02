package com.lzx.myboot;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.lzx.myboot.mapper")
public class MyBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBootApplication.class, args);
    }

}
