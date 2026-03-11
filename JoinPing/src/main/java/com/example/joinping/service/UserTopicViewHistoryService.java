package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;

public interface UserTopicViewHistoryService {
    
    /**
     * 分页查询我的话题浏览记录
     *
     * @param search 携带分页查询的信息
     * @return
     */
    PageResult<Topic> page(Search search);
    
    /**
     * 用户点击某话题后，生成浏览记录
     *
     * @param id
     */
    void insertOrUpdate(Long id);
    
    /**
     * 已知主体id，记录话题的浏览记录
     *
     * @param id
     */
    void insertOrUpdateByThingId(Long id);
    
    /**
     * 清空浏览历史记录
     */
    void delete();
}
