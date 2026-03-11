package com.example.joinping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.pojo.BufferTopic;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BufferTopicMapper extends BaseMapper<BufferTopic> {
    
    /**
     * 获取审核失败话题的图片id
     *
     * @param id
     * @return
     */
    @Select("SELECT photoId FROM buffer_topic WHERE id = #{id}")
    Long getPhotoIdEvenDeleted(@Param("id") Long id);
}
