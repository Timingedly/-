package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.T1Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface T1CommentMapper extends BaseMapper<T1Comment> {
    /**
     * 查询主体下的一级评论列表，热度优先
     *
     * @param thingId
     * @param userId
     * @return
     */
    List<T1Comment> getByThingIdHotFirst(@Param("thingId") Long thingId, @Param("userId") Long userId);
    
    /**
     * 查询主体下的一级评论列表，最新发布优先
     *
     * @param thingId
     * @param userId
     * @return
     */
    List<T1Comment> getByThingIdNewFirst(@Param("thingId") Long thingId, @Param("userId") Long userId);
    
    /**
     * 查询一级评论详情
     *
     * @param id
     * @param userId
     * @return
     */
    T1Comment getById(@Param("id") Long id, @Param("userId") Long userId);
    
    
    /**
     * 查询主体下的一级评论ID列表
     *
     * @param id
     * @param limit
     * @return
     */
    @Select("SELECT id FROM t1comment WHERE thingId = #{id} AND status = 0 LIMIT #{limit}")
    List<Long> getIdListByThingId(@Param("id") Long id, @Param("limit") Integer limit);
    
    /**
     * 更改评论数
     *
     * @param id
     * @param changeValue
     */
    @Update("UPDATE t1comment SET replyNum = replyNum + #{changeValue} WHERE id = #{id} AND status = 0")
    void changeCommentSum(Long id, Integer changeValue);
    
    /**
     * 查询按默认排序后排在目标评论前的评论数量
     *
     * @param id
     * @return
     */
    @Select("SELECT COUNT(*) AS position\n" +
            "FROM joinping.t1comment t1\n" +
            "JOIN joinping.t1comment t2 ON t2.id = #{id}\n" +
            "WHERE t1.thingId = #{thingId}\n" +
            "  AND t1.status = 0\n" +
            "  AND (\n" +
            "    t1.likeNum > t2.likeNum\n" +
            "    OR (t1.likeNum = t2.likeNum AND t1.replyNum > t2.replyNum)\n" +
            "    OR (t1.likeNum = t2.likeNum AND t1.replyNum = t2.replyNum AND t1.id > t2.id)\n" +
            "  )")
    Integer getIndexById(@Param("id") Long id, @Param("thingId") Long thingId);
    
    /**
     * 查询我的一级评论
     *
     * @param userId
     * @return
     */
    @Select("SELECT t1.*,t.name AS thingName FROM t1comment t1 INNER JOIN thing t ON t1.thingId = t.id WHERE t1.userId = #{userId} AND t1.status = 0 ORDER BY t1.id DESC")
    List<T1Comment> selectUserReplyWithThingName(Long userId);
}
