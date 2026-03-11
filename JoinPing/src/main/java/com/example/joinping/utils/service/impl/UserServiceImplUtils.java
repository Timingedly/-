package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.User;
import com.example.joinping.mapper.UserMapper;
import com.example.joinping.utils.service.UserServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplUtils extends ServiceImpl<UserMapper, User> implements UserServiceUtils {
}
