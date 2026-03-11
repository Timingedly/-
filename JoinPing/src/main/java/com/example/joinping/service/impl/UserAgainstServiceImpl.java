package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.pojo.T2Comment;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.UserAgainst;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.*;
import com.example.joinping.service.UserAgainstService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RabbitMQUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserAgainstServiceImpl implements UserAgainstService {
    @Resource
    private UserAgainstMapper userAgainstMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private ThingMapper thingMapper;
    @Resource
    private T1CommentMapper t1CommentMapper;
    @Resource
    private T2CommentMapper t2CommentMapper;
    
    @Transactional
    public void against(UserAgainst userAgainst) {
        //排除虚构信息举报
        if (!objectIsExist(userAgainst.getObjectId(), userAgainst.getType())) {
            throw new BusinessException("要举报的内容不存在或已被删除");
        }
        //排除重复举报
        if (ObjectUtil.isNotNull(userAgainstMapper.getIfExist(StpUtil.getLoginIdAsLong(), userAgainst.getObjectId()))) {
            throw new BusinessException("禁止重复举报");
        }
        OllamaMessage ollamaMessage = new OllamaMessage();
        ollamaMessage.setMessage(getAgainstMessage(userAgainst.getObjectId(), userAgainst.getType()));
        CommonUtils.initRelaPojoCommonProperties(userAgainst);
        //保存举报记录
        userAgainstMapper.insert(userAgainst);
        ollamaMessage.setId(userAgainst.getObjectId());
        ollamaMessage.setUserId(StpUtil.getLoginIdAsLong());
        ollamaMessage.setRoutingKey(userAgainst.getType().toString());
        //异步审核举报
        rabbitMQUtils.sendIdToAgainstDirectExchange(ollamaMessage);
    }
    
    /**
     * 获取目标文本
     *
     * @param id
     * @return
     */
    private String getAgainstMessage(Long id, Integer type) {
        if (type.equals(MessageRoutingKeyEnum.TOPIC.getType())) {
            Topic topic = topicMapper.selectById(id);
            if (ObjectUtil.hasNull(topic, topic.getName(), topic.getContent())) {
                throw new BusinessException("举报的内容不存在或已被删除");
            }
            return topic.getName() + "," + topic.getContent();
        } else if (type.equals(MessageRoutingKeyEnum.THING.getType())) {
            Thing thing = thingMapper.selectById(id);
            if (ObjectUtil.hasNull(thing, thing.getName())) {
                throw new BusinessException("举报的内容不存在或已被删除");
            }
            return thing.getName();
        } else if (type.equals(MessageRoutingKeyEnum.T1COMMENT.getType())) {
            T1Comment t1Comment = t1CommentMapper.selectById(id);
            if (ObjectUtil.hasNull(t1Comment, t1Comment.getContent())) {
                throw new BusinessException("举报的内容不存在或已被删除");
            }
            return t1Comment.getContent();
        } else if (type.equals(MessageRoutingKeyEnum.T2COMMENT.getType())) {
            T2Comment t2Comment = t2CommentMapper.selectById(id);
            if (ObjectUtil.hasNull(t2Comment, t2Comment.getContent())) {
                throw new BusinessException("举报的内容不存在或已被删除");
            }
            return t2Comment.getContent();
        } else {
            throw new BusinessException("举报的内容不存在或已被删除");
        }
    }
    
    /**
     * 校验要举报的内容是否不存在或已被删除
     *
     * @param id
     * @param type
     * @return
     */
    private Boolean objectIsExist(Long id, Integer type) {
        if (ObjectUtil.hasNull(id, type)) {
            return false;
        }
        Map<Integer, BaseMapper> mapperMap = new HashMap<>();
        mapperMap.put(MessageRoutingKeyEnum.TOPIC.getType(), topicMapper);
        mapperMap.put(MessageRoutingKeyEnum.THING.getType(), thingMapper);
        mapperMap.put(MessageRoutingKeyEnum.T1COMMENT.getType(), t1CommentMapper);
        mapperMap.put(MessageRoutingKeyEnum.T2COMMENT.getType(), t2CommentMapper);
        BaseMapper baseMapper = mapperMap.get(type);
        if (ObjectUtil.isNull(baseMapper)) {
            return false;
        }
        if (ObjectUtil.isNull(baseMapper.selectById(id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public Integer get(Long id) {
        if (id == null) {
            return 0;
        }
        UserAgainst userAgainst = userAgainstMapper.getIfExist(StpUtil.getLoginIdAsLong(), id);
        if (ObjectUtil.isNull(userAgainst)) {
            return 0;
        }
        return 1;
    }
}
