package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.relaPojo.TopicUserFavorite;
import com.example.joinping.entity.vo.PageResult;

public interface TopicUserFavoriteService {
    /**
     * 收藏
     *
     * @param topicUserFavorite
     */
    void confirmFavorite(TopicUserFavorite topicUserFavorite);
    
    /**
     * 取消收藏
     *
     * @param topicUserFavorite
     */
    void cancelFavorite(TopicUserFavorite topicUserFavorite);
    
    /**
     * 用户收藏情况
     *
     * @param id
     * @return true已收藏
     */
    Boolean getUserFavoriteStatus(Long id);
    
    /**
     * 分页查询“我的收藏”话题列表
     *
     * @param search
     * @return
     */
    PageResult selectPage(Search search);
}
