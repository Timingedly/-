package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级评论（缓冲）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("buffer_t1comment")
public class BufferT1Comment extends CommonPojo {
    
    /**
     * 所属的主体id
     */
    private Long thingId;
    
    /**
     * 发表评论的用户ID
     */
    private Long userId;
    
    /**
     * 被点赞数
     */
    private Long likeNum;
    
    /**
     * 被回复数
     */
    private Integer replyNum;
    
    /**
     * 评论内容
     */
    private String content;
    
    public Long getLikeNum() {
        if (likeNum == null) {
            return 0L;
        }
        return likeNum;
    }
    
    public Integer getReplyNum() {
        if (replyNum == null) {
            return 0;
        }
        return replyNum;
    }
    
}
