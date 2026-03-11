package com.example.joinping.entity.common;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Pojo的父类
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class CommonPojo implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;
    
    /**
     * 状态
     */
    @TableLogic
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 操作的用户id
     */
    private Long userId;
    
    /**
     * 每页大小
     */
    @TableField(exist = false)
    private Integer pageSize;
    
    /**
     * 目标页数
     */
    @TableField(exist = false)
    private Integer pageNum;
    
    /**
     * 按创建时间倒序排序
     */
    @TableField(exist = false)
    private Boolean orderByCreateTimeDesc;
    
    /**
     * 所在位置（预留给需定位的业务）
     */
    @TableField(exist = false)
    private Integer index;
    
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return pageSize;
    }
    
    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }
    
    public Integer getStatus() {
        if (status == null) {
            return 0;
        }
        return status;
    }
    
    public Boolean getOrderByCreateTimeDesc() {
        return Boolean.TRUE.equals(orderByCreateTimeDesc);
    }
    
    public Integer getIndex() {
        if (index == null) {
            return 0;
        }
        return index;
    }
}
