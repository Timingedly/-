package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户与话题的收藏关系
 */
@Data
@TableName("topic_user_favorite")
@NoArgsConstructor
public class TopicUserFavorite extends RelaCommonPojo {
    /**
     * 话题id
     */
    private Long topicId;
    
    public TopicUserFavorite(Long userId, Integer status, Long topicId, LocalDateTime createTime) {
        super(userId, status, createTime);
        this.topicId = topicId;
    }
}
