package com.example.joinping.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.joinping.constant.ElasticsearchConstants;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.ElasticsearchTopic;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.entity.vo.SearchResult;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.service.IndexService;
import com.example.joinping.utils.ElasticsearchUtils;
import com.example.joinping.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.joinping.constant.RedisConstants.INDEX_KEY_LIST;

@Service
@Slf4j
public class IndexServiceImpl implements IndexService {
    @Resource
    private TopicMapper topicMapper;
    
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ElasticsearchUtils elasticsearchUtils;
    
    
    /**
     * 查找首页热榜话题
     */
    @Override
    public List<Topic> rankingList(String key) {
        if (!INDEX_KEY_LIST.contains(key)) {
            throw new BusinessException("首页传入的排行榜key未定义");
        }
        return redisUtils.getIndexRankList(key);
    }
    
    @Override
    public PageResult search(Search search) {
        if (StrUtil.isBlank(search.getText())) {
            throw new BusinessException("查询文本为空");
        }
        HashMap<String, Float> hashMap = new HashMap<>();
        hashMap.put(ElasticsearchTopic.Fields.name, 2.0F);
        hashMap.put(ElasticsearchTopic.Fields.thingNames, 1.0F);
        SearchResult searchResult = elasticsearchUtils.getIdsByText(
                ElasticsearchConstants.TOPIC_INDEX,
                search.getText(),
                ElasticsearchTopic.class,
                search.getOrderByCreateTimeDesc(),
                hashMap,
                search.getPageNum(),
                search.getPageSize()
        );
        if (searchResult.getTotal() == 0L) {
            return PageResult.convert(new Page<>());
        }
        //在数据库中查出对应的话题，并手动完成排序(数据库中查出的对象与ES中返回的id顺序一致)
        List<Topic> topics = topicMapper.selectBatchIds(searchResult.getIds());
        // 创建ID到对象的映射
        Map<Long, Topic> topicMap = topics.stream().collect(Collectors.toMap(Topic::getId, topic -> topic));
        // 按照原始ID顺序重新排序
        List<Topic> sortedTopics = searchResult.getIds().stream()
                .map(topicMap::get)
                .filter(Objects::nonNull)  // 过滤掉可能不存在的ID
                .collect(Collectors.toList());
        //手动分页
        //构建MyBatis-Plus的Page对象
        Page<Topic> page = new Page<>(search.getPageNum(), search.getPageSize());
        page.setRecords(sortedTopics);
        page.setTotal(searchResult.getTotal());
        return PageResult.convert(page);
    }
}
