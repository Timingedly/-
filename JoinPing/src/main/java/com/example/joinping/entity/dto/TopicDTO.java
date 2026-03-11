package com.example.joinping.entity.dto;

import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 话题DTO
 *
 * @see com.example.joinping.entity.pojo.Topic
 */
@Data
@NoArgsConstructor
public class TopicDTO extends Topic {
    
    /**
     * 其下的主体列表
     */
    private List<Thing> thingList;
}