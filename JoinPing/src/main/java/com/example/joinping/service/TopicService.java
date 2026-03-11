package com.example.joinping.service;

import com.example.joinping.entity.dto.TopicDTO;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferTopic;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;

import java.util.List;

public interface TopicService {
    
    /**
     * 根据话题id获取话题信息
     *
     * @param id
     * @return
     */
    Topic getById(Long id);
    
    
    /**
     * 根据话题id获取话题DTO信息
     *
     * @return
     */
    TopicDTO getDtoById(Long id);
    
    /**
     * 根据分区id分页查询话题列表
     *
     * @param topic
     * @return
     */
    PageResult<Topic> getByAreaId(Topic topic);
    
    /**
     * 新增话题
     *
     * @param bufferTopic
     */
    void insert(BufferTopic bufferTopic);
    
    
    /**
     * 根据id删除话题
     *
     * @param id
     */
    void delete(Long id, Long userId);
    
    /**
     * 新增/更新ES文档
     *
     * @param id
     */
    void insertOrUpdateElasticsearchDocument(Long id);
    
    /**
     * 查询话题类别列表
     *
     * @return
     */
    List getAreaList();
    
    /**
     * 分页查询我创建的话题列表
     *
     * @param search
     * @return
     */
    PageResult getMyTopicList(Search search);
    
}
