package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.joinping.constant.DocumentConstants;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.pojo.User;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.mapper.DocumentMapper;
import com.example.joinping.mapper.UserMapper;
import com.example.joinping.service.UserService;
import com.example.joinping.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private DocumentMapper documentMapper;
    @Value("${limit.max.length.userName}")
    private Integer maxUserNameLength;
    @Value("${limit.max.length.password}")
    private Integer maxPasswordLength;
    
    @Override
    @Transactional
    public Result login(User user) {
        //对账号和密码的校验
        if (!phoneIsValid(user)) {
            return Result.fail("登录失败，账号或密码错误");
        }
        //根据账号查询数据库
        User userInfo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()));
        if (ObjectUtil.isNull(userInfo)) {
            return Result.fail("账号不存在");
        } else {
            //登录账号
            if (!userInfo.getPassword().equals(user.getPassword())) {
                return Result.fail("登录失败，密码错误");
            }
            //更新最近登录时间
            userInfo.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(userInfo);
        }
        //存储token
        StpUtil.login(userInfo.getId());
        return Result.success(userInfo.getId());
    }
    
    /**
     * 校验用户所填信息是否合法
     *
     * @param user
     * @return
     */
    private Boolean phoneIsValid(User user) {
        if (ObjectUtil.hasNull(user, user.getPhone())) {
            log.error("手机号校验错误：手机号为空");
            return false;
        }
        if (String.valueOf(user.getPhone()).length() != 11) {
            log.error("手机号校验错误：手机号位数不是11位");
            return false;
        }
        if (StrUtil.isBlank(user.getPassword())) {
            log.error("密码校验错误：登录用户的密码为空");
            return false;
        }
        if (user.getPassword().length() > maxPasswordLength) {
            log.error("密码校验错误：密码过长");
            return false;
        }
        return true;
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        //注销token
        StpUtil.logout();
        User user = userMapper.selectById(id);
        userMapper.deleteById(id);
        documentMapper.deleteById(user.getPhotoId());
        //用户创建的话题、评论发布后就独立管理了，无需连坐处理
    }
    
    @Override
    public Result register(User user) {
        //对账号和密码的校验
        if (!phoneIsValid(user)) {
            return Result.fail("注册失败，账号或密码错误");
        }
        //根据账号查询数据库
        User userInfo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()));
        //已有账户
        if (ObjectUtil.isNotNull(userInfo)) {
            return Result.fail("账号已存在");
        }
        //新用户注册
        userInfo = new User(user.getPhone(), user.getPassword());
        CommonUtils.initPojoCommonPropertiesWithOutUserId(userInfo);
        userInfo.setName("用户" + userInfo.getId());
        userInfo.setPhotoUrl(DocumentConstants.REQUEST_STATIC_MAPPING_PATH + DocumentConstants.UPLOAD_DEFAULT_IMAGE_PATH + DocumentConstants.USER_DEFAULT_IMAGE_NAME);
        userMapper.insert(userInfo);
        return Result.success("注册成功");
    }
    
    @Override
    public void updateUserName(String name) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("输入用户名为空");
        }
        if (name.length() > maxUserNameLength) {
            throw new BusinessException("用户名超出字数限制");
        }
        User user = new User();
        user.setId(StpUtil.getLoginIdAsLong());
        user.setName(name);
        userMapper.updateById(user);
    }
    
}
