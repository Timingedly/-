package com.example.joinping.Initializer;

import com.example.joinping.constant.ElasticsearchConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 初始化ES各个索引
 */
@Component
@Slf4j
public class ElasticsearchIndexInitializer {
    
    @Resource
    private RestHighLevelClient restHighLevelClient;
    
    /**
     * 自动创建多个索引
     */
    @PostConstruct
    void initIndex() {
        try {
            createIndex(ElasticsearchConstants.TOPIC_INDEX, ElasticsearchConstants.TOPIC_MAPPING_TEMPLATE);
        } catch (IOException e) {
            log.error("Elasticsearch索引创建报错");
        }
    }
    
    /**
     * 根据索引名和索引模板创建索引
     *
     * @param indexName            索引名
     * @param indexMappingTemplate 索引模板
     * @throws IOException
     */
    void createIndex(String indexName, String indexMappingTemplate) throws IOException {
        // 1. 创建判断索引是否存在的请求
        GetIndexRequest getRequest = new GetIndexRequest(indexName);
        // 2. 检查索引是否存在
        boolean exists = restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
        // 3. 如果索引不存在，则创建索引
        if (!exists) {
            CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.source(indexMappingTemplate, XContentType.JSON);
            restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
            System.out.println("ES索引创建成功: " + indexName);
        } else {
            System.out.println("ES索引已存在，跳过创建: " + indexName);
        }
    }
}
