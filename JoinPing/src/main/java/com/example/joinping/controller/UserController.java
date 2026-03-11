package com.example.joinping.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.pojo.User;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.UserService;
import com.example.joinping.utils.service.UserServiceUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关请求接口类
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private UserServiceUtils userServiceUtils;
    
    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping
    public Result get() {
        return Result.success(userServiceUtils.getById(StpUtil.getLoginIdAsLong()));
    }
    
    
    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return
     */
    @GetMapping("/login")
    public Result login(User user) {
        return userService.login(user);
    }
    
    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    @PutMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }
    
    /**
     * 更改用户名
     *
     * @param name
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.UpdateLock)
    @PostMapping
    public Result update(String name) {
        userService.updateUserName(name);
        return Result.success();
    }
    
    
    /**
     * 销毁账户
     *
     * @return
     */
    public Result destoryAccount(Long id) {
        userService.deleteById(id);
        return Result.success();
    }
    
}

