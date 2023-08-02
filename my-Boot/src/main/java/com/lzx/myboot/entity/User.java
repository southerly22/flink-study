package com.lzx.myboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lzx
 * @date 2023/6/8 11:33
 * @description: TODO
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class User {
    private String aid;
    private String aname;
    private String asex;
}
