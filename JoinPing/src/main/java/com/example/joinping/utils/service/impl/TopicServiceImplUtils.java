package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.utils.service.TopicServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class TopicServiceImplUtils extends ServiceImpl<TopicMapper, Topic> implements TopicServiceUtils {
}
