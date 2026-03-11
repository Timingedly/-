package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.vo.PageResult;

public interface NoticeService {
    
    /**
     * 分页查询回复用户回复记录
     *
     * @param search
     * @return
     */
    PageResult getUserReplyNoticePage(Search search);
    
    /**
     * 分页查询系统通知记录
     *
     * @param search
     * @return
     */
    PageResult getSystemNoticePage(Search search);
    
    /**
     * 查询是否有未读新通知(0有1没有)
     *
     * @return
     */
    Integer getStatus();
    
    /**
     * 清空某种类型的通知
     *
     * @param type
     */
    void deleteNotice(Integer type);
    
}
