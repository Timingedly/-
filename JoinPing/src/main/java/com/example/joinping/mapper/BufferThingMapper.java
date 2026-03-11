package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.BufferThing;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BufferThingMapper extends BaseMapper<BufferThing> {
    /**
     * 获取审核失败话题的图片id
     *
     * @param id
     * @return
     */
    @Select("SELECT photoId FROM buffer_thing WHERE id = #{id}")
    Long getPhotoIdEvenDeleted(@Param("id") Long id);
    
}
