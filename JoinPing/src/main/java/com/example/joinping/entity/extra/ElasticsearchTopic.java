package com.example.joinping.entity.extra;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.example.joinping.entity.common.ElasticsearchCommonPojo;
import com.example.joinping.entity.dto.TopicDTO;
import com.example.joinping.entity.pojo.Thing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 话题类（ES专用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class ElasticsearchTopic extends ElasticsearchCommonPojo {
    /**
     * 话题名称
     */
    private String name;
    
    /**
     * 话题关联的多个主体名字列表
     */
    private List<String> thingNames;
    
    
    /**
     * 根据TopicDTO获取Topic索引库的JSON文档内容
     *
     * @param topicDTO
     * @return
     */
    public static String getJSONDocument(TopicDTO topicDTO) {
        ElasticsearchTopic elasticsearchTopic = new ElasticsearchTopic();
        elasticsearchTopic.setId(topicDTO.getId());
        elasticsearchTopic.setName(topicDTO.getName());
        elasticsearchTopic.setThingNames(topicDTO.getThingList().stream().map(Thing::getName).toList());
        elasticsearchTopic.setCreateTime(topicDTO.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return JSONUtil.toJsonStr(elasticsearchTopic);
    }
    
    /**
     * 转换JSON类型为对象
     *
     * @param jsonElasticsearchTopic
     * @return
     */
    public static ElasticsearchTopic getElasticsearchTopicByJSONString(String jsonElasticsearchTopic) {
        return JSONUtil.toBean(jsonElasticsearchTopic, ElasticsearchTopic.class);
    }
    
    /**
     * 获取文档更新所需的字段Map
     *
     * @param topicDTO
     * @return
     */
    public static Map<String, Object> getUpdatePropertiesMap(TopicDTO topicDTO) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Fields.name, topicDTO.getName());
        hashMap.put(Fields.thingNames, topicDTO.getThingList().stream().map(Thing::getName).collect(Collectors.joining(",")));
        return hashMap;
    }
    
    /**
     * 获取批量新增文档所需集合
     *
     * @param indexName
     * @param topicDTOList
     * @return
     */
    public static List<IndexRequest> getBatchIndexRequests(String indexName, List<TopicDTO> topicDTOList) {
        if (CollUtil.isEmpty(topicDTOList)) {
            throw new RuntimeException("传入的话题集合为空");
        }
        List<IndexRequest> indexRequestList = new ArrayList<>();
        for (TopicDTO topicDTO : topicDTOList) {
            IndexRequest indexRequest = new IndexRequest(indexName).id(topicDTO.getId().toString());
            indexRequest.source(getJSONDocument(topicDTO), XContentType.JSON);
            indexRequestList.add(indexRequest);
        }
        return indexRequestList;
    }
}
