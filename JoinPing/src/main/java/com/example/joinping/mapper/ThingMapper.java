package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.Thing;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ThingMapper extends BaseMapper<Thing> {
    /**
     * 获得某话题下的主体id列表
     *
     * @param id
     * @return
     */
    @Select("SELECT id FROM thing WHERE topicId = #{id} AND status = 0")
    List<Long> getIdListByTopicId(Long id);
    
    /**
     * 获取某话题下的主体列表（带平均评分）
     *
     * @param id
     * @return
     */
    @Select("select *, \n" +
            "       CASE \n" +
            "           WHEN customerNum = 0 THEN 0 \n" +
            "           ELSE score/customerNum \n" +
            "       END as averageScore \n" +
            "from thing \n" +
            "where topicId= #{id} and status = 0 ORDER BY id")
    List<Thing> selectThingsWithScoreByTopicId(Long id);
    
    /**
     * 更改评论数
     *
     * @param id
     * @param changeValue
     */
    @Update("UPDATE thing SET commentSum = commentSum + #{changeValue} WHERE id = #{id} AND status = 0")
    void changeCommentSum(Long id, Integer changeValue);
    
    /**
     * 首次评分
     *
     * @param score
     */
    @Update("UPDATE thing SET score = score + #{score} , customerNum = customerNum + 1 WHERE id = #{id} AND status = 0")
    void addScore(@Param("id") Long id, @Param("score") Integer score);
    
    /**
     * 批量删除某话题下的所有主体信息
     *
     * @param topicId
     */
    @Update("UPDATE thing SET status = 1 WHERE topicId = #{topicId}")
    void removeAllByTopicId(Long topicId);
}
