package com.example.joinping.entity.extra;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

/**
 * 搜索信息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Search {
    /**
     * 用户输入的文本内容
     */
    private String text;
    /**
     * 按创建时间倒序排序
     */
    @Transient
    @TableField(exist = false)
    private Boolean orderByCreateTimeDesc;
    /**
     * 目标页码
     */
    private Integer pageNum;
    /**
     * 每页目标记录数
     */
    private Integer pageSize;
    
    
    /**
     * 页码默认为0
     *
     * @return
     */
    public Integer getPageNum() {
        if (ObjectUtil.isNull(pageNum) || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }
    
    /**
     * 每页记录数默认为10
     *
     * @return
     */
    public Integer getPageSize() {
        if (ObjectUtil.isNull(pageSize) || pageSize < 1) {
            return 10;
        }
        return pageSize;
    }
    
    public Boolean getOrderByCreateTimeDesc() {
        return Boolean.TRUE.equals(orderByCreateTimeDesc);
    }
}
