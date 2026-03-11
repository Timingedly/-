package com.example.joinping.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.joinping.constant.NoticeConstants;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.pojo.Notice;
import com.example.joinping.entity.pojo.T2Comment;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.enums.NoticeTypeEnum;
import com.example.joinping.enums.StatusEnum;
import com.example.joinping.mapper.NoticeMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.utils.service.TopicServiceUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.joinping.constant.LuaScriptConstants.*;
import static com.example.joinping.constant.RedisConstants.*;

/**
 * Redis工具类
 */
@Component
@Slf4j
public class RedisUtils {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private TopicServiceUtils topicServiceUtils;
    @Resource
    private ThingMapper thingMapper;
    @Resource
    private NoticeMapper noticeMapper;
    
    /**
     * 重置定时任务 - 所有实例共享
     */
    public void resetSchedule() {
        // 直接更新Redis中的目标时间
        redisTemplate.opsForValue().set(KEY_SCHEDULE_TIME,
                System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
    }
    
    /**
     * 尝试开启/关闭热榜更新(5分钟分布式锁)
     *
     * @param trueOnFalseOff
     */
    public Boolean turnOnOrTurnOffUpdate(boolean trueOnFalseOff) {
        if (trueOnFalseOff) {
            //热榜开始更新,尝试设置分布式锁
            // 1 = 成功获取锁，0 = 锁已存在
            Long result = (Long) redisTemplate.execute(indexUpdateLock(), List.of(KEY_UPDATE_SIGN), LOCK_EXPIRE_SECOND);
            return ObjectUtil.equals(result, 1L);
        } else {
            //热榜更新完毕
            redisTemplate.delete(KEY_UPDATE_SIGN);
            return true;
        }
    }
    
    /**
     * 判断某话题是否是热榜话题
     *
     * @param id
     * @return
     */
    public Boolean existTopic(Long id) {
        Object result = redisTemplate.execute(getIndexTopic(), List.of(KEY_UPDATE_SIGN, getKeyTopic(false, id), getKeyTopic(true, id)));
        return ObjectUtil.isNotNull(result);
    }
    
    
    /**
     * 获取首页热榜话题
     *
     * @param id
     * @return
     */
    public Topic getIndexTopicByTopicId(Long id) {
        //key,active,buffer
        String result = (String) redisTemplate.execute(getIndexTopic(), List.of(KEY_UPDATE_SIGN, getKeyTopic(false, id), getKeyTopic(true, id)));
        if (ObjectUtil.isNull(result)) {
            return null;
        }
        return JSONUtil.toBean(result, Topic.class);
    }
    
    /**
     * 获取热榜排行榜
     *
     * @param id
     * @return
     */
    public List<Topic> getIndexRankList(String aim) {
        //key,active,buffer
        List result = null;
        if (KEY_WEEK_MOST_LIKE.equals(aim)) {
            result = (List) redisTemplate.execute(indexRankList(), List.of(KEY_UPDATE_SIGN, getKeyWeekMostLike(false), getKeyWeekMostLike(true)));
        } else if (KEY_WEEK_MOST_FAVORITE.equals(aim)) {
            result = (List) redisTemplate.execute(indexRankList(), List.of(KEY_UPDATE_SIGN, getKeyWeekMostFavorite(false), getKeyWeekMostFavorite(true)));
        }//可扩展
        
        List<Topic> topicList = new ArrayList<>();
        if (CollUtil.isEmpty(result)) {
            return topicList;
        }
        for (Object obj : result) {
            topicList.add((Topic) obj);
        }
        return topicList;
    }
    
    /**
     * (取消)点赞/(取消)收藏热榜话题
     *
     * @param id
     * @return
     */
    public void updateIndexTopicLikeOrFavoriteNum(Long id, boolean trueLike_falseFavorite, boolean trueConfirm_falseCancel) {
        //key,active,buffer
        int likeNumUpdate = 0, favoriteNumUpdate = 0;
        if (trueLike_falseFavorite) {
            if (trueConfirm_falseCancel) {
                //点赞
                likeNumUpdate = 1;
            } else {
                //取消点赞
                likeNumUpdate = -1;
            }
        } else {
            if (trueConfirm_falseCancel) {
                //收藏
                favoriteNumUpdate = 1;
            } else {
                //取消收藏
                favoriteNumUpdate = -1;
            }
        }
        redisTemplate.execute(
                updateIndexTopicHash(),
                List.of(
                        KEY_UPDATE_SIGN,
                        getKeyTopicLikeOrFavoriteNum(false, id),
                        getKeyTopicLikeOrFavoriteNum(true, id),
                        Topic.Fields.likeNum,
                        Topic.Fields.favoriteNum
                ),
                likeNumUpdate,
                favoriteNumUpdate
        );
    }
    
    
    /**
     * 删除首页热榜话题详情缓存
     */
    public void deleteIndexTopicByTopicId(Long id) {
        redisTemplate.execute(deleteKeys(), List.of(
                getKeyTopic(false, id),
                getKeyTopic(true, id),
                getKeyTopicLikeOrFavoriteNum(false, id),
                getKeyTopicLikeOrFavoriteNum(true, id))
        );
    }
    
    /**
     * 存储用户回复通知并返回
     *
     * @param t2Comment
     * @return
     */
    @Transactional
    public Notice updateNoticeWhenReply(T2Comment t2Comment) {
        Notice notice = new Notice();
        CommonUtils.initPojoCommonPropertiesWithOutUserId(notice);
        notice.setUserId(t2Comment.getReplyUserId());
        notice.setFromUserId(t2Comment.getUserId());
        notice.setObjectId(t2Comment.getId());
        notice.setType(NoticeTypeEnum.User.getValue());
        notice.setHasRead(StatusEnum.NORMAL.getValue());
        notice.setContent(NoticeConstants.getReplyContent(t2Comment.getUserId()));
        noticeMapper.insert(notice);
        redisTemplate.opsForValue().set(KEY_NOTICE + notice.getUserId(), StatusEnum.NORMAL.getValue());
        return notice;
    }
    
    /**
     * 存储系统通知并返回
     *
     * @param ollamaMessage
     * @return
     */
    @Transactional
    public Notice updateNoticeWhenSystemNotPass(OllamaMessage ollamaMessage, String noticeContent) {
        Notice notice = new Notice();
        CommonUtils.initPojoCommonPropertiesWithOutUserId(notice);
        notice.setUserId(ollamaMessage.getUserId());
        notice.setFromUserId(ollamaMessage.getUserId());
        notice.setObjectId(ollamaMessage.getId());
        notice.setType(NoticeTypeEnum.System.getValue());
        notice.setHasRead(StatusEnum.NORMAL.getValue());
        notice.setContent(noticeContent);
        noticeMapper.insert(notice);
        redisTemplate.opsForValue().set(KEY_NOTICE + notice.getUserId(), StatusEnum.NORMAL.getValue());
        return notice;
    }
    
    /**
     * 新增或删除用户通知标志key
     *
     * @param forDelete
     */
    public void insertOrDeleteNotice(boolean forDelete, Long userId) {
        if (forDelete) {
            redisTemplate.delete(KEY_NOTICE + userId);
            return;
        }
        Notice notice = noticeMapper.selectTheNewest(userId);
        int status = StatusEnum.NORMAL.getValue();
        if (ObjectUtil.isNotNull(notice)) {
            status = notice.getHasRead();
        }
        //缓存当前用户新通知接收状态
        redisTemplate.opsForValue().set(KEY_NOTICE + userId, status);
    }
    
    /**
     * 用户是否已在WebSocket注册且还在线
     *
     * @param userId
     * @return
     */
    public Boolean userIsAlive(Long userId) {
        Object sth = redisTemplate.opsForValue().get(KEY_NOTICE + userId);
        return ObjectUtil.isNotNull(sth);
    }
    
    
    /**
     * 删除某个path下所有key
     *
     * @param path
     */
    public void deleteKeysByPath(String path) {
        Set<String> keys = redisTemplate.keys(path + "*");
        if (CollUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }
    
    /**
     * 将Redis中的热榜数据同步到Mysql中
     *
     * @param redisKey
     */
    @Transactional
    public void turnRedisDataToMysql(String redisKey) {
        // 1. 使用keys方法匹配所有 redisKey 开头的键
        Set<String> keys = redisTemplate.keys(redisKey + "*");
        if (CollUtil.isEmpty(keys)) {
            //第一次初始化数据时从这跳过
            System.out.println("redis热榜数据中未找到匹配的键");
            return;
        }
        ArrayList<Topic> topicList = new ArrayList<>();
        // 2. 遍历所有匹配到的键(每个键是[路径+id]的组合)
        for (String key : keys) {
            // 从key中提取topicId，而不是直接解析整个key为Long
            String topicIdStr = key.substring(key.lastIndexOf(":") + 1);
            // 验证提取的字符串是否是有效的数字
            if (!NumberUtil.isNumber(topicIdStr)) {
                log.info("无效的topicId格式: " + key);
                continue;
            }
            // 3. 使用entries方法获取此键对应的全部哈希数据
            Topic topic = topicMapper.selectById(Long.parseLong(topicIdStr));
            Map<Object, Object> hashData = redisTemplate.opsForHash().entries(key);
            // 4. 遍历并处理哈希数据
            for (Map.Entry<Object, Object> entry : hashData.entrySet()) {
                String hashKey = (String) entry.getKey();   // 哈希字段
                Long value = ((Integer) entry.getValue()).longValue();
                if (Topic.Fields.likeNum.equals(hashKey)) {
                    topic.setLikeNum(topic.getLikeNum() + value);
                } else if (Topic.Fields.favoriteNum.equals(hashKey)) {
                    topic.setFavoriteNum(topic.getFavoriteNum() + value);
                }
            }
            topicList.add(topic);
        }
        topicServiceUtils.updateBatchById(topicList);
    }
    
    /**
     * 将source路径下的所有key与数据一模一样复制到target路径下
     * 使用Redis DUMP/RESTORE命令，保证数据完全一致
     *
     * @param source 源路径
     * @param target 目标路径
     */
    public void copyRedisPath(String source, String target) {
        if (!StrUtil.isAllNotBlank(source, target)) {
            throw new IllegalArgumentException("source和target路径不能为空");
        }
        // 规范化路径，确保以:结尾
        String sourcePath = normalizePath(source);
        String targetPath = normalizePath(target);
        if (sourcePath.equals(targetPath)) {
            throw new IllegalArgumentException("source和target路径不能相同");
        }
        // 获取源路径下的所有key
        Set<String> sourceKeys = scanKeys(sourcePath + "*");
        for (String sourceKey : sourceKeys) {
            // 计算目标key
            String targetKey = sourceKey.replace(sourcePath, targetPath);
            copyKeyWithDumpRestore(sourceKey, targetKey);
        }
    }
    
    /**
     * 使用Redis DUMP/RESTORE命令复制单个key的数据和过期时间
     * 这种方法能保证数据完全一致，包括所有数据类型和序列化格式
     */
    private void copyKeyWithDumpRestore(String sourceKey, String targetKey) {
        // 获取key的序列化数据
        byte[] dumpedData = redisTemplate.dump(sourceKey);
        if (dumpedData == null) {
            return; // key不存在或已过期
        }
        
        // 获取过期时间（毫秒）
        Long ttl = redisTemplate.getExpire(sourceKey, TimeUnit.MILLISECONDS);
        
        // 还原数据到目标key
        if (ttl == null) {
            // key不存在（理论上不会执行到这里，因为dump为null时已返回）
            return;
        } else if (ttl == -1) {
            // 永不过期，设置ttl为0
            redisTemplate.restore(targetKey, dumpedData, 0, TimeUnit.MILLISECONDS);
        } else if (ttl == -2) {
            // key不存在（理论上不会执行到这里）
            return;
        } else if (ttl > 0) {
            // 有过期时间
            redisTemplate.restore(targetKey, dumpedData, ttl, TimeUnit.MILLISECONDS);
        } else {
            // ttl == 0 或其他情况，按永不过期处理
            redisTemplate.restore(targetKey, dumpedData, 0, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * 规范化路径，确保以:结尾
     */
    private String normalizePath(String path) {
        if (path.endsWith(":")) {
            return path;
        }
        return path + ":";
    }
    
    /**
     * 使用scan命令安全地获取匹配的key，避免阻塞Redis
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions()
                        .match(pattern)
                        .count(100) // 每次扫描100个
                        .build()
        );
        while (cursor.hasNext()) {
            keys.add(cursor.next());
        }
        try {
            cursor.close();
        } catch (Exception e) {
            // 忽略关闭异常
        }
        return keys;
    }
}
