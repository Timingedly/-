package com.example.joinping.background;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.joinping.constant.NoticeConstants;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.pojo.*;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.*;
import com.example.joinping.service.*;
import com.example.joinping.utils.RedisUtils;
import com.example.joinping.utils.WebSocketServer;
import com.example.joinping.utils.service.T1CommentServiceUtils;
import com.example.joinping.utils.service.T2CommentServiceUtils;
import com.example.joinping.utils.service.ThingServiceUtils;
import com.example.joinping.utils.service.TopicServiceUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.joinping.constant.RabbitMQConstants.*;
import static com.example.joinping.constant.RedisConstants.*;

/**
 * 消息队列RabbitMQ的消费者
 */
@Component
@Slf4j
@Transactional
public class RabbitMQListener implements SmartLifecycle {
    /**
     * 运行权限变量
     */
    private volatile boolean running = false;
    @Resource
    private OllamaApiService ollamaApiService;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private BufferTopicMapper bufferTopicMapper;
    @Resource
    private ThingMapper thingMapper;
    @Resource
    private BufferThingMapper bufferThingMapper;
    @Resource
    private T1CommentMapper t1CommentMapper;
    @Resource
    private BufferT1CommentMapper bufferT1CommentMapper;
    @Resource
    private T2CommentMapper t2CommentMapper;
    @Resource
    private BufferT2CommentMapper bufferT2CommentMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TopicServiceUtils topicServiceUtils;
    @Resource
    private ThingServiceUtils thingServiceUtils;
    @Resource
    private T1CommentServiceUtils t1CommentServiceUtils;
    @Resource
    private T2CommentServiceUtils t2CommentServiceUtils;
    @Resource
    private TopicService topicService;
    @Resource
    private ThingService thingService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private T1CommentService t1CommentService;
    @Resource
    private T2CommentService t2CommentService;
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private DocumentService documentService;
    
    /**
     * 存储并发送用户回复通知
     *
     * @param t2Comment
     */
    private void loadAndSendNoticeWhenReply(T2Comment t2Comment) {
        Notice notice = redisUtils.updateNoticeWhenReply(t2Comment);
        WebSocketServer.sendOneText(notice.getUserId(), notice.getContent());
    }
    
    /**
     * 存储并发送系统通知
     *
     * @param ollamaMessage
     * @param noticeContent
     */
    private void loadAndSendNoticeWhenSystemNotPass(OllamaMessage ollamaMessage, String noticeContent) {
        Notice notice = redisUtils.updateNoticeWhenSystemNotPass(ollamaMessage, noticeContent);
        WebSocketServer.sendOneText(notice.getUserId(), notice.getContent());
    }
    
    
    /**
     * 监听普通队列并调用ollama对Message携带的文本进行审核(用作测试)
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = NORMAL_QUEUE)
    public void listenNormalQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        System.out.println("======================================");
        System.out.println("======================================");
        try {
            long beforeAnalyse = System.currentTimeMillis();
            String jsonResponseByMessage = ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage());
            Boolean finalReviewResult = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), jsonResponseByMessage);
            long afterAnalyse = System.currentTimeMillis();
            System.out.println("分析总耗时为: " + (afterAnalyse - beforeAnalyse) + " 毫秒,审核结果为: " + finalReviewResult);
            // 消息确认 - 处理成功，手动确认
            channel.basicAck(tag, false);
        } catch (IOException e) {
            // 处理异常，拒绝消息并重新入队
            channel.basicNack(tag, false, true);
            System.out.println("处理异常，消息已重新入队: " + e.getMessage());
        }
        System.out.println("======================================");
        System.out.println("======================================");
        
    }
    
    /**
     * 监听topic队列并调用ollama对Message携带的文本进行审核
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = TOPIC_QUEUE)
    public void listenTopicQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Topic topic = analyseMessageAndConvertRecord(
                    ollamaMessage,
                    MessageRoutingKeyEnum.TOPIC.getValue(),
                    new Topic(),
                    topicServiceUtils,
                    bufferTopicMapper
            );
            if (ObjectUtil.hasNull(topic, topic.getId())) {
                //审核失败，丢弃该消息
                documentService.deleteById(bufferTopicMapper.getPhotoIdEvenDeleted(ollamaMessage.getId()));
                loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.TOPIC_NOT_PASS);
                channel.basicAck(tag, false);
                return;
            }
            //审核通过了，在ES上新增文档
            topicService.insertOrUpdateElasticsearchDocument(topic.getId());
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 监听thing队列并调用ollama对Message携带的文本进行审核
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = THING_QUEUE)
    public void listenThingQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Thing thing = analyseMessageAndConvertRecord(
                    ollamaMessage,
                    MessageRoutingKeyEnum.THING.getValue(),
                    new Thing(),
                    thingServiceUtils,
                    bufferThingMapper
            );
            if (ObjectUtil.hasNull(thing, thing.getId(), thing.getTopicId())) {
                //审核失败，丢弃该消息
                documentService.deleteById(bufferThingMapper.getPhotoIdEvenDeleted(ollamaMessage.getId()));
                loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.THING_NOT_PASS);
                channel.basicAck(tag, false);
                return;
            }
            //审核通过了，在ES上新增or更新文档
            topicService.insertOrUpdateElasticsearchDocument(thing.getTopicId());
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 监听t1comment队列并调用ollama对Message携带的文本进行审核
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = T1COMMENT_QUEUE)
    public void listenT1CommentQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            T1Comment t1Comment = analyseMessageAndConvertRecord(
                    ollamaMessage,
                    MessageRoutingKeyEnum.T1COMMENT.getValue(),
                    new T1Comment(),
                    t1CommentServiceUtils,
                    bufferT1CommentMapper
            );
            if (ObjectUtil.hasNull(t1Comment, t1Comment.getId(), t1Comment.getThingId())) {
                //数据异常，丢弃该消息
                loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.COMMENT_NOT_PASS);
                channel.basicAck(tag, false);
                return;
            }
            //所属主体评论数+1
            thingMapper.changeCommentSum(t1Comment.getThingId(), 1);
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 监听t2comment队列并调用ollama对Message携带的文本进行审核
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = T2COMMENT_QUEUE)
    public void listenT2CommentQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            T2Comment t2Comment = analyseMessageAndConvertRecord(
                    ollamaMessage,
                    MessageRoutingKeyEnum.T2COMMENT.getValue(),
                    new T2Comment(),
                    t2CommentServiceUtils,
                    bufferT2CommentMapper
            );
            if (ObjectUtil.hasNull(t2Comment, t2Comment.getId(), t2Comment.getT1commentId(), t2Comment.getThingId())) {
                //数据异常，丢弃该消息
                loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.COMMENT_NOT_PASS);
                channel.basicAck(tag, false);
                return;
            }
            //回复的一级评论的被回复数以及所属评论区的总评论数+1
            t1CommentMapper.changeCommentSum(t2Comment.getT1commentId(), 1);
            thingMapper.changeCommentSum(t2Comment.getThingId(), 1);
            loadAndSendNoticeWhenReply(t2Comment);
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 监听评论清理队列，逻辑删除某主体下所有评论
     *
     * @param thingIdList
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = THING_ID_QUEUE)
    public void listenThingIdQueue(List<Long> thingIdList, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        //每批次处理的评论数
        int limit = 500;
        try {
            //依次清空每个主体的评论区
            for (Long thingId : thingIdList) {
                //分批删除评论，避免一次性给数据库过大压力
                while (true) {
                    List<Long> t1CommentIdList = t1CommentMapper.getIdListByThingId(thingId, limit);
                    if (CollUtil.isEmpty(t1CommentIdList)) {
                        break;
                    }
                    //先删除每条一级评论下的二级评论
                    for (Long t1CommentId : t1CommentIdList) {
                        //二级评论直接删，不用分批次
                        t2CommentMapper.deleteByT1CommentId(t1CommentId);
                    }
                    t1CommentMapper.deleteBatchIds(t1CommentIdList);
                    //处理完最后一批一级评论
                    if (t1CommentIdList.size() < limit) {
                        break;
                    }
                }
            }
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 审核对象携带的消息，根据审核结果处理缓存记录
     *
     * @param ollamaMessage              封装的消息对象
     * @param messageRoutingKeyEnumValue 消息类型
     * @param pojo                       正式表对象
     * @param pojoService                正式表service对象
     * @param bufferPojoMapper           缓存表mapper对象
     * @param <T>                        正式表类型
     * @param <U>                        缓存表类型
     * @param <M>                        正式表mapper类型
     * @param <B>                        缓存表mapper类型
     * @return 审核结果
     */
    private <T, U, M extends IService<T>, B extends BaseMapper<U>> T analyseMessageAndConvertRecord(OllamaMessage ollamaMessage, String messageRoutingKeyEnumValue, T pojo, M pojoService, B bufferPojoMapper) {
        if (!messageRoutingKeyEnumValue.equals(ollamaMessage.getRoutingKey())) {
            log.error("传入的OllamaMessage文本对象类型与所处队列类型不符");
            return null;
        }
        Boolean finalReviewResult = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage()));
        //审核通过则将该记录插入正式表中
        if (finalReviewResult) {
            //根据id查出当前记录
            U bufferPojo = bufferPojoMapper.selectById(ollamaMessage.getId());
            //往正式表插入该记录
            BeanUtil.copyProperties(bufferPojo, pojo);
            pojoService.saveOrUpdate(pojo);
        }
        //逻辑删除缓冲表的原记录
        bufferPojoMapper.deleteById(ollamaMessage.getId());
        return pojo;
    }
    
    /**
     * 将首页展示的话题更新并存入redis中（redis的zSet当score相等时按字典序排序）
     *
     * @param currentTimeMillis
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = INDEX_RANKLIST_UPDATE_QUEUE)
    public void indexTopicUpdate(Long currentTimeMillis, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        //开启更新开关，启用备用数据
        redisUtils.turnOnOrTurnOffUpdate(true);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Topic> page = new Page<>(1, 10);
        //查询1周内最多点赞的话题
        List<Topic> weekLikeList = topicMapper.selectPage(page, new LambdaQueryWrapper<Topic>().ge(Topic::getCreateTime, oneWeekAgo).orderByDesc(Topic::getLikeNum).orderByDesc(Topic::getId)).getRecords();
        INDEX_KEY_MAP.put(KEY_WEEK_MOST_LIKE, weekLikeList);
        //查询1周内最多收藏的话题
        List<Topic> weekFavoList = topicMapper.selectPage(page, new LambdaQueryWrapper<Topic>().ge(Topic::getCreateTime, oneWeekAgo).orderByDesc(Topic::getFavoriteNum).orderByDesc(Topic::getId)).getRecords();
        INDEX_KEY_MAP.put(KEY_WEEK_MOST_FAVORITE, weekFavoList);
        try {
            //将增量数据同步到Mysql中
            redisUtils.turnRedisDataToMysql(getPathTopicLikeOrFavoriteNum(false));
            //删除正式榜单数据
            redisUtils.deleteKeysByPath(PATH_INDEX_RANKLIST_ACTIVE);
            //更新正式榜单数据
            Map<String, Long> map = MapBuilder.create(new HashMap<String, Long>()).put(Topic.Fields.likeNum, 0L).put(Topic.Fields.favoriteNum, 0L).build();
            for (String key : INDEX_KEY_LIST) {
                List<Topic> topicList = INDEX_KEY_MAP.get(key);
                int index = 1;
                for (Topic topic : topicList) {
                    // 添加到zSet
                    redisTemplate.opsForZSet().add(PATH_INDEX_RANKLIST_ACTIVE + key, topic, index++);
                    redisTemplate.opsForValue().setIfAbsent(PATH_INDEX_RANKLIST_ACTIVE + topic.getId().toString(), topic);
                    //保存Hash
                    redisTemplate.opsForHash().putAll(getKeyTopicLikeOrFavoriteNum(false, topic.getId()), map);
                }
            }
            //恢复正式数据
            redisUtils.turnOnOrTurnOffUpdate(false);
            //将备用数据也同步到数据库中（更新正式数据期间产生的变化）
            redisUtils.turnRedisDataToMysql(getPathTopicLikeOrFavoriteNum(true));
            //删除备用榜单数据
            redisUtils.deleteKeysByPath(PATH_INDEX_RANKLIST_BUFFER);
            //将正式数据复制一份作为备用数据
            redisUtils.copyRedisPath(PATH_INDEX_RANKLIST_ACTIVE, PATH_INDEX_RANKLIST_BUFFER);
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 受理对Topic的举报
     *
     * @param ollamaMessage
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = AGAINST_TOPIC_QUEUE)
    public void listenAgainstTopicQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Boolean result = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage()));
            //审核失败
            if (!result) {
                //删除话题
                topicService.delete(ollamaMessage.getId(), ollamaMessage.getUserId());
            }
            loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.getTopicAgainstResult(!result));
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 受理对Thing的举报
     *
     * @param id
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = AGAINST_THING_QUEUE)
    public void listenAgainstThingQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Boolean result = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage()));
            //审核失败
            if (!result) {
                //删除主体
                thingService.delete(ollamaMessage.getId(), ollamaMessage.getUserId());
            }
            loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.getThingAgainstResult(!result));
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 受理对T1Comment的举报
     *
     * @param id
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = AGAINST_T1COMMENT_QUEUE)
    public void listenAgainstT1CommentQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Boolean result = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage()));
            //审核失败
            if (!result) {
                //删除
                t1CommentService.delete(ollamaMessage.getId(), ollamaMessage.getUserId());
            }
            loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.getCommentAgainstResult(!result));
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    /**
     * 受理对T2Comment的举报
     *
     * @param id
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = AGAINST_T2COMMENT_QUEUE)
    public void listenAgainstT2CommentQueue(OllamaMessage ollamaMessage, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            Boolean result = ollamaApiService.getFinalReviewResultByResponse(ollamaMessage.getMessage(), ollamaApiService.getJSONResponseByMessage(ollamaMessage.getMessage()));
            //审核失败
            if (!result) {
                //删除
                t2CommentService.delete(ollamaMessage.getId(), ollamaMessage.getUserId());
            }
            loadAndSendNoticeWhenSystemNotPass(ollamaMessage, NoticeConstants.getCommentAgainstResult(!result));
            channel.basicAck(tag, false);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw e;
        } catch (Exception e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
    
    
    @Override
    public void start() {
        log.info("RabbitMQListener 开始启动，确保所有服务都已就绪...");
        // 可以在这里添加健康检查
        this.running = true;
        log.info("RabbitMQListener 启动完成，开始接收消息");
    }
    
    @Override
    public void stop() {
        log.info("RabbitMQListener 停止");
        this.running = false;
    }
    
    @Override
    public boolean isRunning() {
        return this.running;
    }
    
    @Override
    public int getPhase() {
        // 返回最大值，确保在所有其他组件之后启动
        return Integer.MAX_VALUE;
    }
    
    /**
     * 删除本地保存的图片
     *
     * @param path
     * @param tag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = IMAGE_PATH_QUEUE)
    public void listenImageIdQueue(Long id, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) throws IOException {
        try {
            //直接删除
            Document document = documentMapper.selectById(id);
            Assert.notNull(document, "文件不存在");
            if (StrUtil.isBlank(document.getPath())) {
                throw new BusinessException("文件路径不存在");
            }
            //先逻辑删除对应记录
            documentMapper.deleteById(id);
            Files.delete(Path.of(document.getPath()));
            channel.basicAck(tag, false);
        } catch (IOException e) {
            //数据异常，丢弃该消息
            channel.basicAck(tag, false);
            throw e;
        }
    }
}
