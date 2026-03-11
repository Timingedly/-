package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户与话题的点赞关系
 */
@Data
@TableName("topic_user_like")
@NoArgsConstructor
public class TopicUserLike extends RelaCommonPojo {
    /**
     * 话题id
     */
    private Long topicId;
    
    public TopicUserLike(Long userId, Long topicId, Integer status, LocalDateTime createTime) {
        super(userId, status, createTime);
        this.topicId = topicId;
    }
}
