package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;

import java.util.List;


public interface IndexService {
    
    /**
     * 根据Key获取排行榜
     *
     * @param key redis中zSet的key
     * @return
     */
    List<Topic> rankingList(String key);
    
    /**
     * 根据用户输入文本查询相关话题
     *
     * @param search
     * @return
     */
    PageResult search(Search search);
    
    
}
