package com.lzx.myboot.mapper;

import com.lzx.myboot.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author lzx
 * @date 2023/6/8 11:47
 * @description: TODO
 */

public interface UserMapper {
    @Select("select * from tableln")
    List<User> selectAll();

    @Select("select * from tableln where aname = #{name}")
    User selectByName(@Param("name") String name);
}
