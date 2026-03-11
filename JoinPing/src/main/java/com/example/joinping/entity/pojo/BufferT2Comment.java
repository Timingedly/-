package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二级评论（缓冲）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("buffer_t2comment")
public class BufferT2Comment extends CommonPojo {
    
    /**
     * 回复的一级评论ID
     */
    private Long t1commentId;
    
    /**
     * 回复的用户ID
     */
    private Long replyUserId;
    
    /**
     * 发表评论的用户ID
     */
    private Long userId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 所在主体id
     */
    private Long thingId;
    
    /**
     * 区分回复的是一级评论还是二级评论
     */
    private Integer replyType;
}
