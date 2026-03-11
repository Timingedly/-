package com.example.joinping.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.joinping.constant.RabbitMQConstants.*;

/**
 * RabbitMQ工具类
 */
@Component
@Slf4j
public class RabbitMQUtils {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisUtils redisUtils;
    
    /**
     * 往普通direct交换机发送OllamaMessage对象
     *
     * @param ollamaMessage
     */
    public void sendOllamaMessageToNormalDirectExchange(OllamaMessage ollamaMessage) {
        if (ObjectUtil.isNull(ollamaMessage)) {
            throw new BusinessException("要发送的 ollamaMessage 对象为空");
        }
        rabbitTemplate.convertAndSend(NORMAL_DIRECT_EXCHANGE, ollamaMessage.getRoutingKey(), ollamaMessage);
    }
    
    /**
     * 往评论清理交换机发送主体id
     *
     * @param thingId
     */
    public void sendThingIdToThingIdFanoutExchange(Long thingId, List<Long> thingIdList) {
        //没有主体，不需要清理评论区
        if (thingId == null && CollUtil.isEmpty(thingIdList)) {
            return;
        }
        if (thingId == null) {
            rabbitTemplate.convertAndSend(THING_ID_FANOUT_EXCHANGE, null, thingIdList);
        } else {
            rabbitTemplate.convertAndSend(THING_ID_FANOUT_EXCHANGE, null, List.of(thingId));
        }
    }
    
    /**
     * 异步更新首页话题热榜
     */
    public void sendCommondToIndexRankListUpdateExchange() {
        //重置定时任务
        redisUtils.resetSchedule();
        //传达执行命令
        rabbitTemplate.convertAndSend(INDEX_RANKLIST_UPDATE_FANOUT_EXCHANGE, null, System.currentTimeMillis());
    }
    
    /**
     * 异步受理举报
     *
     * @param ollamaMessage
     * @param routingKey
     */
    public void sendIdToAgainstDirectExchange(OllamaMessage ollamaMessage) {
        rabbitTemplate.convertAndSend(AGAINST_DIRECT_EXCHANGE, ollamaMessage.getRoutingKey(), ollamaMessage);
    }
    
    /**
     * 异步删除本地图片
     *
     * @param path
     */
    public void sendPhotoPathToPhotoPathFanoutExchange(Long id) {
        rabbitTemplate.convertAndSend(IMAGE_PATH_FANOUT_EXCHANGE, null, id);
    }
}
