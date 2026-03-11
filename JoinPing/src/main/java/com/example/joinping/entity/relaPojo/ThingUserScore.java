package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户与主体的评分关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName
public class ThingUserScore extends RelaCommonPojo {
    /**
     * 主体id
     */
    private Long thingId;
    /**
     * 用户评分
     */
    private Integer score;
    
    /**
     * 用户评分（多少颗星星）
     */
    @TableField(exist = false)
    private Integer starNum;
    
    public ThingUserScore(Long userId, Integer status, Long thingId, Integer score, LocalDateTime createTime) {
        super(userId, status, createTime);
        this.thingId = thingId;
        this.score = score;
    }
    
    public Integer getStarNum() {
        if (score != null) {
            return score % 2;
        }
        return 0;
    }
}
