package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.relaPojo.T1CommentUserLike;
import com.example.joinping.entity.relaPojo.TopicUserLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface T1CommentUserLikeMapper extends BaseMapper<T1CommentUserLike> {
    /**
     * 逻辑删除已有点赞记录
     *
     * @param t1CommentId
     * @param userId
     */
    @Update("UPDATE t1comment_user_like SET status = 1 WHERE t1CommentId = #{t1CommentId} AND userId = #{userId}")
    Integer delete(@Param("t1CommentId") Long t1CommentId, @Param("userId") Long userId);
    
    /**
     * 查询是否有过点赞记录
     *
     * @param t1CommentId
     * @param userId
     * @return
     */
    @Select("SELECT * FROM t1comment_user_like WHERE t1CommentId = #{t1CommentId} AND userId = #{userId}")
    TopicUserLike selectIgnoreStatus(@Param("t1CommentId") Long t1CommentId, @Param("userId") Long userId);
    
    /**
     * 恢复点赞状态
     *
     * @param t1CommentId
     * @param userId
     */
    @Update("UPDATE t1comment_user_like SET status = 0,createTime = #{createTime} WHERE t1CommentId = #{t1CommentId} AND userId = #{userId} AND status = 1")
    Integer insertByUpdate(@Param("t1CommentId") Long t1CommentId, @Param("userId") Long userId, @Param("createTime") LocalDateTime createTime);
}
