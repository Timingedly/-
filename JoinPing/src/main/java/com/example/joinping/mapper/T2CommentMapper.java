package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.T2Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface T2CommentMapper extends BaseMapper<T2Comment> {
    /**
     * 查询一级评论下的二级评论（按发布时间顺序）
     *
     * @param id
     * @return
     */
    List<T2Comment> getByT1CommentId(Long id);
    
    /**
     * 查询一级评论下的二级评论ID列表
     *
     * @param id
     * @param limit
     * @return
     */
    @Select("SELECT id FROM t2comment WHERE t1CommentId = #{id} AND status = 0 LIMIT #{limit}")
    List<Long> getIdListByT1CommentId(@Param("id") Long id, @Param("limit") Integer limit);
    
    /**
     * 逻辑删除一级评论下的所有二级评论
     *
     * @param id
     */
    @Update("UPDATE t2comment SET status = 1 WHERE t1commentId = #{id}")
    void deleteByT1CommentId(Long id);
    
    /**
     * 查询按默认排序后排在目标评论前的评论数量
     *
     * @param id
     * @return
     */
    @Select("SELECT COUNT(*) FROM t2comment where id < #{id} AND t1CommentId = #{t1CommentId} AND status = 0")
    Integer getIndexById(@Param("id") Long id, @Param("t1CommentId") Long t1CommentId);
    
    /**
     * 确保主体和一级评论都未被删除
     *
     * @param thingId
     * @param t1CommentId
     * @return
     */
    @Select("SELECT COUNT(*) FROM thing t \n" +
            "JOIN t1comment c ON t.id = c.thingId \n" +
            "WHERE t.id = #{thingId} AND c.id = #{t1CommentId} AND t.status = 0 AND c.status = 0")
    Integer checkT1CommentIsExisting(@Param("thingId") Long thingId, @Param("t1CommentId") Long t1CommentId);
    
    /**
     * 查询用户
     *
     * @param userId
     * @return
     */
    @Select("SELECT t2.*,u.name AS replyUserName FROM t2comment t2 INNER JOIN user u ON t2.replyUserId = u.id WHERE t2.userId = #{userId} AND t2.status = 0 ORDER BY t2.id DESC")
    List<T2Comment> selectUserReplyWithReplyUserName(Long userId);
}
