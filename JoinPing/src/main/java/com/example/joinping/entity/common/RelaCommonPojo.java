package com.example.joinping.entity.common;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 用户行为相关Pojo的父类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelaCommonPojo {
    
    /**
     * 用户id
     */
    @TableField(value = "userId", jdbcType = JdbcType.BIGINT)
    private Long userId;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    public Integer getStatus() {
        if (status == null) {
            return 0;
        }
        return status;
    }
    
}
