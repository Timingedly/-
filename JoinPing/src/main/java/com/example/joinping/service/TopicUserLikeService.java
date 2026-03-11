package com.example.joinping.service;

import com.example.joinping.entity.relaPojo.TopicUserLike;

public interface TopicUserLikeService {
    /**
     * 点赞
     *
     * @param topicUserLike
     */
    void confirmLike(TopicUserLike topicUserLike);
    
    /**
     * 取消点赞
     *
     * @param topicUserLike
     */
    void cancelLike(TopicUserLike topicUserLike);
    
    /**
     * 用户点赞情况
     *
     * @param topicUserLike
     * @return true已点赞
     */
    Boolean userLiked(TopicUserLike topicUserLike);
}
