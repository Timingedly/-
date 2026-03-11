package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.relaPojo.UserAgainst;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserAgainstMapper extends BaseMapper<UserAgainst> {
    
    /**
     * 查询是否已有举报记录
     *
     * @param userId
     * @param objectId
     * @return
     */
    @Select("SELECT * FROM user_against WHERE userId= #{userId} AND objectId = #{objectId} AND status = 0")
    UserAgainst getIfExist(@Param("userId") Long userId, @Param("objectId") Long objectId);
}
