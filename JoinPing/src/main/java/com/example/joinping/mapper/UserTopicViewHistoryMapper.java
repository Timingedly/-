package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.UserTopicViewHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface UserTopicViewHistoryMapper extends BaseMapper<UserTopicViewHistory> {
    
    /**
     * 返回用户的话题浏览记录
     *
     * @param userId
     * @return
     */
    @Select("SELECT t2.* , t1.createTime as lastViewTime\n" +
            "FROM user_topic_view_history AS t1\n" +
            "INNER JOIN topic AS t2 ON t1.topicId = t2.id\n" +
            "WHERE t1.userId = #{userId} \n" +
            "  AND t1.status = 0 \n" +
            "  AND t2.status = 0\n" +
            "ORDER BY t1.createTime DESC")
    List<Topic> list(Long userId);
    
    /**
     * 新增/更新话题浏览记录
     *
     * @param topicId
     * @param userId
     * @param updateTime
     */
    @Update("UPDATE user_topic_view_history SET createTime = #{updateTime} , status = 0 WHERE userId = #{userId} AND topicId=#{topicId}")
    void updateUpdateTime(@Param("topicId") Long topicId, @Param("userId") Long userId, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 查询是否曾经浏览过指定话题（无论是否删除浏览记录）
     *
     * @param topicId
     * @param userId
     * @return
     */
    @Select("SELECT * FROM user_topic_view_history WHERE userId = #{userId} AND topicId = #{topicId}")
    UserTopicViewHistory getAnyStatus(@Param("topicId") Long topicId, @Param("userId") Long userId);
    
}
