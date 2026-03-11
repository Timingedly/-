package com.example.joinping.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ES配置类
 */
@Configuration
public class ElasticsearchConfig {
    @Value("${elasticsearch.address}")
    private String ElasticsearchAddress;
    
    /**
     * 创建 RestHighLevelClient Bean
     * 应用启动时自动初始化，应用结束时自动关闭
     */
    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create(ElasticsearchAddress)
                )
        );
    }
    
}
