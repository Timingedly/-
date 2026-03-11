package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User extends CommonPojo {
    /**
     * (冗余)微信用户唯一标识
     */
    private String openId;
    /**
     * (冗余)微信开放平台统一id（用户在多平台的唯一标识）
     */
    private String unionId;
    /**
     * 账号(手机号)
     */
    private Long phone;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户名
     */
    private String name;
    /**
     * 图片文件id
     */
    private Long photoId;
    
    /**
     * 话题图片路径
     */
    private String photoUrl;
    
    /**
     * 最后一次登录的时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 根据账号，密码创建新用户
     *
     * @param phone    账号
     * @param password 密码
     */
    public User(Long phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
