package com.example.joinping.service;

import com.example.joinping.entity.pojo.BufferThing;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;

public interface ThingService {
    /**
     * 根据id查询主体
     */
    Thing getById(Long id);
    
    
    /**
     * 删除主体
     *
     * @param id
     */
    void delete(Long id, Long userId);
    
    /**
     * 根据话题id分页查询主体列表
     *
     * @param topic
     * @return
     */
    PageResult selectPage(Topic topic);
    
    /**
     * 新增主体
     *
     * @param bufferThing
     */
    void insert(BufferThing bufferThing);
}
