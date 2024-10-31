package com.bjfu.mapper;


import com.bjfu.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {


    @Insert("INSERT INTO user(account, password) VALUES(#{account}, #{password})")
    void register(User user);

    @Select("SELECT count(*) FROM user WHERE account = #{account} AND password = #{password}")
    Integer login(User user);

    @Select("SELECT count(*) FROM user WHERE account = #{account}")
    Integer count(User user);

    @Insert("INSERT INTO user(account, password) VALUES(#{account}, #{password})")
    void insert(User user);
}
