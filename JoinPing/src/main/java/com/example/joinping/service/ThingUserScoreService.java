package com.example.joinping.service;

import com.example.joinping.entity.relaPojo.ThingUserScore;

public interface ThingUserScoreService {
    
    /**
     * 获取用户对主体的评分
     *
     * @param thingUserScore
     * @return 返回0则未评分，正常1-5
     */
    Integer getUserScore(ThingUserScore thingUserScore);
    
    /**
     * 对主体进行评分
     *
     * @param thingUserScore
     */
    void insertOrUpdateScore(ThingUserScore thingUserScore);
}
