package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.TopicUserFavorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface TopicUserFavoriteMapper extends BaseMapper<TopicUserFavorite> {
    /**
     * 逻辑删除已有收藏记录
     *
     * @param topicId
     * @param userId
     */
    @Update("UPDATE topic_user_favorite SET status = 1 WHERE topicId = #{topicId} AND userId = #{userId}")
    Integer delete(@Param("topicId") Long topicId, @Param("userId") Long userId);
    
    /**
     * 查询是否有过点赞记录
     *
     * @param topicId
     * @param userId
     * @return
     */
    @Select("SELECT * FROM topic_user_favorite WHERE topicId = #{topicId} AND userId = #{userId}")
    TopicUserFavorite selectIgnoreStatus(@Param("topicId") Long topicId, @Param("userId") Long userId);
    
    /**
     * 恢复点赞状态
     *
     * @param topicId
     * @param userId
     */
    @Update("UPDATE topic_user_favorite SET status = 0,createTime = #{createTime} WHERE topicId = #{topicId} AND userId = #{userId} AND status = 1")
    Integer insertByUpdate(@Param("topicId") Long topicId, @Param("userId") Long userId, @Param("createTime") LocalDateTime createTime);
    
    /**
     * 分页查询“我的收藏”话题列表
     *
     * @param userId
     * @return
     */
    @Select("SELECT t.* FROM topic AS t INNER JOIN topic_user_favorite f ON t.id = f.topicId WHERE f.userId = #{userId} AND t.status = 0 AND f.status = 0")
    List<Topic> selectTopicPage(@Param("userId") Long userId);
}
