package com.example.joinping.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * 与ES交互的pojo的父类
 */
@Data
@FieldNameConstants
@NoArgsConstructor
public class ElasticsearchCommonPojo {
    /**
     * 话题id
     */
    private Long id;
    
    /**
     * 话题创建时间的时间戳
     */
    private Long createTime;
}
