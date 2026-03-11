package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.relaPojo.TopicUserLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface TopicUserLikeMapper extends BaseMapper<TopicUserLike> {
    /**
     * 逻辑删除已有点赞记录
     *
     * @param topicId
     * @param userId
     */
    @Update("UPDATE topic_user_like SET status = 1 WHERE topicId = #{topicId} AND userId = #{userId}")
    Integer delete(@Param("topicId") Long topicId, @Param("userId") Long userId);
    
    /**
     * 查询是否有过点赞记录
     *
     * @param topicId
     * @param userId
     * @return
     */
    @Select("SELECT * FROM topic_user_like WHERE topicId = #{topicId} AND userId = #{userId}")
    TopicUserLike selectIgnoreStatus(@Param("topicId") Long topicId, @Param("userId") Long userId);
    
    /**
     * 恢复点赞状态
     *
     * @param topicId
     * @param userId
     */
    @Update("UPDATE topic_user_like SET status = 0 ,createTime = #{createTime} WHERE topicId = #{topicId} AND userId = #{userId} AND status = 1")
    Integer insertByUpdate(@Param("topicId") Long topicId, @Param("userId") Long userId, @Param("createTime") LocalDateTime createTime);
}
