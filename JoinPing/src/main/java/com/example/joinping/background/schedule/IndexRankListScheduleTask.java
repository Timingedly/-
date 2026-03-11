package com.example.joinping.background.schedule;

import com.example.joinping.utils.RabbitMQUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.joinping.constant.RedisConstants.KEY_SCHEDULE_LOCK;
import static com.example.joinping.constant.RedisConstants.KEY_SCHEDULE_TIME;

/**
 * 首页热榜更新定时任务(多实例同步)
 */
@Component
public class IndexRankListScheduleTask {
    @Resource
    private RedisTemplate<String, Long> redisTemplate;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    
    
    /**
     * 定时任务检查与执行
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) //项目启动后立即执行第一次，然后每隔1小时执行一次（60分钟 * 60秒 * 1000毫秒）
    public void checkAndExecute() {
        // 获取分布式锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(KEY_SCHEDULE_LOCK, 1L, 1, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 从Redis获取共享的目标时间
                Long oldAimTime = redisTemplate.opsForValue().get(KEY_SCHEDULE_TIME);
                // 检查是否到达执行时间
                if (oldAimTime == null || System.currentTimeMillis() >= oldAimTime) {
                    // 执行任务
                    rabbitMQUtils.sendCommondToIndexRankListUpdateExchange();
                }
            } finally {
                // 释放锁
                redisTemplate.delete(KEY_SCHEDULE_LOCK);
            }
        }
    }
}
