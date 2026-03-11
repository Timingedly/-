package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.joinping.entity.dto.TopicDTO;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.pojo.BufferThing;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.BufferThingMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.service.ThingService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.ElasticsearchUtils;
import com.example.joinping.utils.RabbitMQUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ThingServiceImpl implements ThingService {
    @Resource
    private ThingMapper thingMapper;
    @Resource
    private BufferThingMapper bufferThingMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private ElasticsearchUtils elasticsearchUtils;
    @Value("${limit.max.length.thingName}")
    private Integer maxNameLength;
    
    @Override
    public Thing getById(Long id) {
        Assert.notNull(id, "主体id为空");
        //根据id查询主体信息
        Thing thing = thingMapper.selectById(id);
        Assert.notNull(thing, "id为" + id + "的主体不存在");
        return thing;
    }
    
    @Override
    public void insert(BufferThing bufferThing) {
        if (ObjectUtil.hasNull(bufferThing, bufferThing.getName(), topicMapper.selectById(bufferThing.getTopicId()))) {
            throw new BusinessException("主体信息有误");
        }
        if (bufferThing.getName().length() > maxNameLength) {
            throw new BusinessException("主体名超出字数限制");
        }
        CommonUtils.initPojoCommonProperties(bufferThing);
        bufferThingMapper.insert(bufferThing);
        rabbitMQUtils.sendOllamaMessageToNormalDirectExchange(new OllamaMessage(
                bufferThing.getId(),
                MessageRoutingKeyEnum.THING.getValue(),
                bufferThing.getName(),
                StpUtil.getLoginIdAsLong()
        ));
    }
    
    @Override
    public void delete(Long id, Long userId) {
        Assert.notNull(id, "主体id为空");
        Thing thing = thingMapper.selectById(id);
        Assert.notNull(thing, "主体为空");
        if (ObjectUtil.notEqual(userId, thing.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        //删除数据库中记录
        thingMapper.deleteById(id);
        //删除对应图片
        rabbitMQUtils.sendPhotoPathToPhotoPathFanoutExchange(thing.getPhotoId());
        //清空评论区
        rabbitMQUtils.sendThingIdToThingIdFanoutExchange(id, null);
        //更新es中文档信息
        Topic topic = topicMapper.selectById(thing.getTopicId());
        TopicDTO topicDTO = new TopicDTO();
        BeanUtil.copyProperties(topic, topicDTO);
        topicDTO.setThingList(thingMapper.selectList(new LambdaQueryWrapper<Thing>().eq(Thing::getTopicId, topic.getId())));
        elasticsearchUtils.insertOrUpdateTopicDocument(topicDTO);
    }
    
    @Override
    public PageResult selectPage(Topic topic) {
        if (ObjectUtil.hasNull(topic, topic.getId(), topicMapper.selectById(topic.getId()))) {
            throw new BusinessException("查询的话题不存在");
        }
        Page<Thing> thingPage = thingMapper.selectPage(new Page<>(topic.getPageNum(), topic.getPageSize()), new LambdaQueryWrapper<Thing>().eq(Thing::getTopicId, topic.getId()).orderByAsc(Thing::getId));
        return PageResult.convert(thingPage);
    }
    
}
