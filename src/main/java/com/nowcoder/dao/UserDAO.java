package com.nowcoder.dao;

import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDAO {

    @Insert("insert into user(name,password,salt,head_url) values(" +
            "#{name},#{password},#{salt},#{headUrl})")
    int addUser(User user);

    @Select("select id,name,password,salt,head_url from user where id=#{id}")
    User selectById(int id);

    @Update("update user set password=#{password} where id=#{id}")
    int updatePassword(User user);

    @Delete("delete from user where id=#{id}")
    void deleteById(int id);
}
