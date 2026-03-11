package com.example.joinping.service;

import com.example.joinping.entity.relaPojo.T1CommentUserLike;

public interface T1CommentUserLikeService {
    
    /**
     * 点赞一级评论
     *
     * @param t1CommentUserLike
     */
    void confirmLike(T1CommentUserLike t1CommentUserLike);
    
    /**
     * 取消点赞一级评论
     *
     * @param t1CommentUserLike
     */
    void cancelLike(T1CommentUserLike t1CommentUserLike);
}
