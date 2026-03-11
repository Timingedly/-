package com.example.joinping.service;

import com.example.joinping.entity.pojo.User;
import com.example.joinping.entity.vo.Result;

public interface UserService {
    /**
     * 用户登录
     *
     * @param user
     */
    Result login(User user);
    
    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    Result register(User user);
    
    /**
     * 更改用户名
     *
     * @param name
     */
    void updateUserName(String name);
    
    
    /**
     * 根据用户id注销用户
     *
     * @param id
     * @return
     */
    void deleteById(Long id);
}
