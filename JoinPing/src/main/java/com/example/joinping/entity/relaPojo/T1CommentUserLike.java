package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户与一级评论的点赞关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t1comment_user_like")
public class T1CommentUserLike extends RelaCommonPojo {
    /**
     * 一级评论id
     */
    private Long t1CommentId;
    
    public T1CommentUserLike(Long userId, Integer status, Long t1CommentId, LocalDateTime createTime) {
        super(userId, status, createTime);
        this.t1CommentId = t1CommentId;
    }
}
