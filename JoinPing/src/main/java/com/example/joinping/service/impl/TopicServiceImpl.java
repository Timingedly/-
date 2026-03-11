package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.joinping.entity.dto.TopicDTO;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferTopic;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.AreaEnum;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.BufferTopicMapper;
import com.example.joinping.mapper.DocumentMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.service.TopicService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.ElasticsearchUtils;
import com.example.joinping.utils.RabbitMQUtils;
import com.example.joinping.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicServiceImpl implements TopicService {
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private ThingMapper thingMapper;
    @Resource
    private BufferTopicMapper bufferTopicMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Resource
    private ElasticsearchUtils elasticsearchUtils;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private DocumentMapper documentMapper;
    @Value("${limit.max.length.topicName}")
    private Integer maxNameLength;
    @Value("${limit.max.length.topicContent}")
    private Integer maxContentLength;
    
    @Override
    public Topic getById(Long id) {
        //根据id查询话题信息
        Assert.notNull(id, "传入的话题id为null");
        //是热榜话题直接从缓存返回
        if (redisUtils.existTopic(id)) {
            return redisUtils.getIndexTopicByTopicId(id);
        }
        Topic topic = topicMapper.selectById(id);
        Assert.notNull(topic, "id为" + id + "的话题不存在");
        return topic;
    }
    
    @Override
    public TopicDTO getDtoById(Long id) {
        Topic topic = getById(id);
        TopicDTO topicDTO = new TopicDTO();
        BeanUtil.copyProperties(topic, topicDTO);
        //查询该话题下的主体列表
        topicDTO.setThingList(thingMapper.selectThingsWithScoreByTopicId(id));
        return topicDTO;
    }
    
    @Override
    public PageResult<Topic> getByAreaId(Topic topic) {
        if (!AreaEnum.containsValue(topic.getAreaId())) {
            throw new BusinessException("非法话题类别");
        }
        Page<Topic> topicPage = new Page<>(topic.getPageNum(), topic.getPageSize());
        LambdaQueryWrapper<Topic> lambdaQueryWrapper = new LambdaQueryWrapper<Topic>().eq(Topic::getAreaId, topic.getAreaId());
        if (topic.getOrderByCreateTimeDesc()) {
            //按"最新"排序
            lambdaQueryWrapper.orderByDesc(Topic::getId);
        } else {
            //按"最热"排序
            lambdaQueryWrapper.orderByDesc(Topic::getLikeNum).orderByDesc(Topic::getFavoriteNum).orderByDesc(Topic::getId);
        }
        Page<Topic> page = topicMapper.selectPage(topicPage, lambdaQueryWrapper);
        return PageResult.convert(page);
    }
    
    @Override
    @Transactional
    public void insert(BufferTopic bufferTopic) {
        Assert.notNull(bufferTopic, "传入话题为空");
        if (!AreaEnum.containsValue(bufferTopic.getAreaId())) {
            throw new BusinessException("非法话题类别");
        }
        if (StrUtil.hasBlank(bufferTopic.getName(), bufferTopic.getContent())) {
            throw new BusinessException("话题名或简介为空");
        }
        if (bufferTopic.getName().length() > maxNameLength || bufferTopic.getContent().length() > maxContentLength) {
            throw new BusinessException("话题名或话题简介超出字数限制");
        }
        CommonUtils.initPojoCommonProperties(bufferTopic);
        bufferTopicMapper.insert(bufferTopic);
        //审核内容
        rabbitMQUtils.sendOllamaMessageToNormalDirectExchange(new OllamaMessage(
                bufferTopic.getId(),
                MessageRoutingKeyEnum.TOPIC.getValue(),
                bufferTopic.getName() + "," + bufferTopic.getContent(),
                StpUtil.getLoginIdAsLong()
        ));
    }
    
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Topic oldTopic = topicMapper.selectById(id);
        if (ObjectUtil.notEqual(userId, oldTopic.getUserId())) {
            throw new BusinessException("无权限删除该话题");
        }
        //如果是热榜话题则从缓存中删除并异步更新缓存
        if (redisUtils.existTopic(id)) {
            redisUtils.deleteIndexTopicByTopicId(id);
            rabbitMQUtils.sendCommondToIndexRankListUpdateExchange();
        }
        //删除es文档，不再被搜索到
        elasticsearchUtils.deleteDocumentById(id);
        List<Thing> thingList = thingMapper.selectList(new LambdaQueryWrapper<Thing>().eq(Thing::getTopicId, id));
        List<Long> thingIdList = new ArrayList<>();
        List<Long> thingPhotoIdList = new ArrayList<>();
        for (Thing thing : thingList) {
            thingIdList.add(thing.getId());
            thingPhotoIdList.add(thing.getPhotoId());
        }
        //将话题删除
        topicMapper.deleteById(id);
        if (CollUtil.isNotEmpty(thingList)) {
            //将该话题下的全部主体都删除
            thingMapper.deleteBatchIds(thingIdList);
            //将该话题下的全部主体关联的图片删除
            documentMapper.deleteBatchIds(thingPhotoIdList);
        }
        //将话题关联的图片文件删除
        documentMapper.deleteById(oldTopic.getPhotoId());
        //情空话题下所有主体的评论区
        rabbitMQUtils.sendThingIdToThingIdFanoutExchange(null, thingIdList);
    }
    
    @Override
    @Transactional
    public void insertOrUpdateElasticsearchDocument(Long id) {
        elasticsearchUtils.insertOrUpdateTopicDocument(getDtoById(id));
    }
    
    @Override
    public List getAreaList() {
        return Arrays.stream(AreaEnum.values())
                .map(area -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", area.getId());
                    map.put("description", area.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult getMyTopicList(Search search) {
        Assert.notNull(search, "分页参数异常");
        Page<Topic> topicPage = topicMapper.selectPage(
                new Page<>(search.getPageNum(), search.getPageSize()),
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(Topic::getId)
        );
        return PageResult.convert(topicPage);
    }
}
