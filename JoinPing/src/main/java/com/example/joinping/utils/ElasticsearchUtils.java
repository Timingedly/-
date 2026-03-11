package com.example.joinping.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.example.joinping.constant.ElasticsearchConstants;
import com.example.joinping.entity.common.ElasticsearchCommonPojo;
import com.example.joinping.entity.dto.TopicDTO;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.ElasticsearchTopic;
import com.example.joinping.entity.vo.SearchResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ES工具类
 */
@Slf4j
@Component
public class ElasticsearchUtils {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    
    /**
     * 新增文档
     *
     * @param indexName    索引库名字
     * @param id           索引库id
     * @param jsonDocument JSON格式的文档内容
     */
    public void insertDocument(String indexName, Long id, String jsonDocument) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName).id(id.toString());
            indexRequest.source(jsonDocument, XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new BusinessException("es新增文档出错");
        }
    }
    
    /**
     * 根据indexName和id查询JSON类型的文档
     *
     * @param indexName 索引库名称
     * @param id        索引库id
     * @return JSON格式的文档字符串
     */
    public String getDocumentById(String indexName, Long id) {
        try {
            //创建request对象
            GetRequest getRequest = new GetRequest(indexName, id.toString());
            //发送请求，得到结果
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            //解析结果
            String jsonResponse = getResponse.getSourceAsString();
            return jsonResponse;
        } catch (IOException e) {
            throw new BusinessException("es查询文档出错");
        }
    }
    
    /**
     * 根据indexName和id判断ES中是否已存在该文档
     *
     * @param indexName 索引库名称
     * @param id        索引库id
     * @return 文档是否存在
     */
    public boolean contains(String indexName, Long id) {
        try {
            // 创建request对象
            GetRequest getRequest = new GetRequest(indexName, id.toString());
            // 发送请求，得到结果
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            // 通过isExists方法判断文档是否存在
            return getResponse.isExists();
        } catch (IOException e) {
            throw new BusinessException("es查询文档出错");
        }
    }
    
    /**
     * 更新文档
     *
     * @param indexName        索引库名称
     * @param id               索引库id
     * @param updateProperties 要更新的字段映射Map
     */
    public void updateDocumentById(String indexName, Long id, Map<String, Object> updateProperties) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(indexName, id.toString());
            //指定要更新的字段以及对应的新值
            updateRequest.doc(updateProperties);
            //发送更新请求
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new BusinessException("es更新文档出错");
        }
    }
    
    /**
     * 删除文档
     *
     * @param id
     */
    public void deleteDocumentById(Long id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(ElasticsearchConstants.TOPIC_INDEX, id.toString());
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new BusinessException("es删除文档出错");
        }
    }
    
    /**
     * 批量新增文档
     *
     * @param indexName
     * @param indexRequestList 各个esPojo类中获取的请求对象列表
     */
    public void batchInsertDocument(String indexName, List<IndexRequest> indexRequestList) {
        try {
            if (CollUtil.isEmpty(indexRequestList)) {
                throw new RuntimeException("传入的IndexRequest集合为空");
            }
            BulkRequest bulkRequest = new BulkRequest(indexName);
            for (IndexRequest indexRequest : indexRequestList) {
                bulkRequest.add(indexRequest);
            }
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new BusinessException("es批量新增文档出错");
        }
    }
    
    /**
     * 根据传入文本分页查询相关文档的id列表
     *
     * @param indexName              索引库名
     * @param text                   要搜索的文本
     * @param elasticsearchPojoClass es通用Pojo类
     * @param orderByCreateTimeDesc  是否按"最新"查询
     * @param fieldWithWeight        查询权重配比表
     * @return 搜索结果集
     */
    public <T extends ElasticsearchCommonPojo> SearchResult getIdsByText(String indexName, String text, Class<T> elasticsearchPojoClass, Boolean orderByCreateTimeDesc, Map<String, Float> fieldWithWeight, Integer pageNum, Integer pageSize) {
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            //准备DSL
            searchRequest.source().query(QueryBuilders.multiMatchQuery(text).fields(fieldWithWeight));
            //按"最新"查询，否则按默认的"相关度"查询
            if (Boolean.TRUE.equals(orderByCreateTimeDesc)) {
                searchRequest.source().sort(ElasticsearchCommonPojo.Fields.createTime, SortOrder.DESC);
            }
            //分页
            searchRequest.source().from((pageNum - 1) * pageSize).size(pageSize);
            // 打印DSL
            log.info("DSL: {}", searchRequest.source().toString());
            //发送请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析响应
            SearchHits searchHits = searchResponse.getHits();
            //获取总条数
            long total = searchHits.getTotalHits().value;
            //获取文档数组
            SearchHit[] hits = searchHits.getHits();
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < hits.length; i++) {
                //通过文档JSON获取对应的Bean
                T bean = JSONUtil.toBean(hits[i].getSourceAsString(), elasticsearchPojoClass);
                idList.add(bean.getId());
            }
            return new SearchResult(idList, total);
        } catch (IOException e) {
            throw new BusinessException("es根据传入文本查询id列表出错");
        }
    }
    
    /**
     * 获得文本的分词列表
     *
     * @param text 要分词的文本
     * @return 分词后的多个文本组成的集合
     */
    public List<String> getTestAnalyzeList(String text) {
        try {
            AnalyzeRequest analyzeRequest = AnalyzeRequest.withGlobalAnalyzer(ElasticsearchConstants.ANALYZER_NAME, text);
            AnalyzeResponse response = restHighLevelClient.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
            //从响应中提取分词结果，并收集为 List<String>
            return response.getTokens().stream()
                    .map(AnalyzeResponse.AnalyzeToken::getTerm)
                    .toList();
        } catch (IOException e) {
            throw new BusinessException("es调用分词器对文本分词出错");
        }
    }
    
    /**
     * 新增或更新topic索引库的文档
     *
     * @param topicDTO
     */
    public void insertOrUpdateTopicDocument(TopicDTO topicDTO) {
        if (ObjectUtil.isNull(topicDTO)) {
            return;
        }
        if (contains(ElasticsearchConstants.TOPIC_INDEX, topicDTO.getId())) {
            //更新文档
            updateDocumentById(
                    ElasticsearchConstants.TOPIC_INDEX,
                    topicDTO.getId(),
                    ElasticsearchTopic.getUpdatePropertiesMap(topicDTO)
            );
        } else {
            //新增文档
            insertDocument(
                    ElasticsearchConstants.TOPIC_INDEX,
                    topicDTO.getId(),
                    ElasticsearchTopic.getJSONDocument(topicDTO)
            );
        }
    }
}
