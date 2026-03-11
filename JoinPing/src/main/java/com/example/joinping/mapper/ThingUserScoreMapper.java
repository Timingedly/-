package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.relaPojo.ThingUserScore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface ThingUserScoreMapper extends BaseMapper<ThingUserScore> {
    
    @Update("UPDATE thing_user_score SET score = #{score},createTime=#{createTime} WHERE thingId = #{thingId} AND userId = #{userId} AND status = 0 ")
    void updateScore(@Param("thingId") Long thingId, @Param("userId") Long userId, @Param("score") Integer score, @Param("createTime") LocalDateTime createTime);
}
