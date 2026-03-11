package com.example.joinping.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.joinping.entity.common.CommonPojo;
import com.example.joinping.entity.common.RelaCommonPojo;
import com.example.joinping.enums.StatusEnum;

import java.time.LocalDateTime;

/**
 * 通用工具类
 */
public class CommonUtils {
    /**
     * 初始化对象属性
     *
     * @param commonPojo
     */
    public static void initPojoCommonProperties(CommonPojo commonPojo) {
        commonPojo.setId(DistributedIdGenerator.nextId());
        commonPojo.setStatus(StatusEnum.NORMAL.getValue());
        commonPojo.setUserId(StpUtil.getLoginIdAsLong());
        commonPojo.setCreateTime(LocalDateTime.now());
        commonPojo.setUpdateTime(commonPojo.getCreateTime());
    }
    
    /**
     * 初始化对象属性
     *
     * @param commonPojo
     */
    public static void initPojoCommonPropertiesWithOutUserId(CommonPojo commonPojo) {
        commonPojo.setId(DistributedIdGenerator.nextId());
        commonPojo.setStatus(StatusEnum.NORMAL.getValue());
        commonPojo.setCreateTime(LocalDateTime.now());
        commonPojo.setUpdateTime(commonPojo.getCreateTime());
    }
    
    /**
     * 初始化Rela对象属性
     *
     * @param relaCommonPojo
     */
    public static void initRelaPojoCommonProperties(RelaCommonPojo relaCommonPojo) {
        relaCommonPojo.setUserId(StpUtil.getLoginIdAsLong());
        relaCommonPojo.setStatus(StatusEnum.NORMAL.getValue());
        relaCommonPojo.setCreateTime(LocalDateTime.now());
    }
    
    /**
     * 获取分布式id
     *
     * @return
     */
    public static Long getDistrubutedId() {
        return DistributedIdGenerator.nextId();
    }
    
    /**
     * 校验用户是否有操作权限
     *
     * @param userId 操作目标对象的用户id
     */
    public static Boolean userRightCheck(Long userId) {
        return ObjectUtil.equals(StpUtil.getLoginIdAsLong(), userId);
    }
}
