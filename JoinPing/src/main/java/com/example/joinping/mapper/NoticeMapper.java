package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.Notice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {
    
    /**
     * 查询用户回复通知记录
     *
     * @param userId
     * @param type
     * @return
     */
    @Select("SELECT n.*, u.name AS fromUserName \n" +
            "FROM notice n\n" +
            "INNER JOIN user u ON n.fromUserId = u.id AND u.status = 0\n" +
            "WHERE n.userId = #{userId} AND n.type = #{type} AND n.status = 0 ORDER BY n.id DESC")
    List<Notice> getPageWithUserName(@Param("userId") Long userId, @Param("type") Integer type);
    
    /**
     * 更新是否已读状态
     *
     * @param userId
     * @param hasRead
     */
    @Update("UPDATE notice SET hasRead = #{hasRead} WHERE id = (SELECT tmp.id FROM (SELECT id FROM notice WHERE userId = #{userId} AND status = 0 ORDER BY id DESC LIMIT 1) AS tmp)")
    void updateHasRead(@Param("userId") Long userId, @Param("hasRead") Integer hasRead);
    
    /**
     * 查询最新的回复通知
     *
     * @param userId
     * @return
     */
    @Select("SELECT * FROM notice WHERE userId = #{userId} AND status = 0 ORDER BY id DESC LIMIT 1")
    Notice selectTheNewest(Long userId);
}
