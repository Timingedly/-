package com.example.joinping.Initializer;

import com.example.joinping.config.RedisConfig;
import com.example.joinping.constant.RedisConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Component;

/**
 * 初始化Redis布隆过滤器
 */
@Component
public class BloomFilterInitializer {
    @Resource
    private RBloomFilter<String> bloomFilter;
    
    @Resource
    private RedisConfig redisConfig;
    
    /**
     * 对布隆过滤器进行数据预热
     */
    @PostConstruct
    public void initBloomFilter() {
        try {
            //先删除已有的敏感词集合
            if (bloomFilter.isExists()) {
                bloomFilter.delete();
            }
            bloomFilter.tryInit(RedisConstants.FILTER_WORD_SET.size(), redisConfig.fpp);
            
            for (String element : RedisConstants.FILTER_WORD_SET) {
                bloomFilter.add(element);
            }
            System.out.println("Redis布隆过滤器初始化完成，元素数量: " + RedisConstants.FILTER_WORD_SET.size());
        } catch (Exception e) {
            throw new RuntimeException("Redis布隆过滤器数据预热出错", e);
        }
    }
    
    
}
