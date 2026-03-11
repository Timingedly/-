package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户与话题的浏览记录关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_topic_view_history")
public class UserTopicViewHistory extends RelaCommonPojo {
    /**
     * 话题id
     */
    private Long topicId;
    
    public UserTopicViewHistory(Long userId, Integer status, Long topicId, LocalDateTime createTime) {
        super(userId, status, createTime);
        this.topicId = topicId;
    }
}
