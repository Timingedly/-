package com.example.joinping.constant;

import com.example.joinping.entity.pojo.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Redis相关常量类
 */
public class RedisConstants {
    /**
     * 分布式锁存活时间（5分钟）
     */
    public static final Integer LOCK_EXPIRE_SECOND = 300;
    /**
     * 要过滤的敏感词集合
     */
    public static final Set<String> FILTER_WORD_SET = Set.of(
            "将评"
    );
    /**
     * [project]项目名
     */
    public static final String PROJECT_NAME = "joinping";
    /**
     * 点赞操作的分布式锁
     */
    public static final String PATH_AOPLOCK_LIKE = PROJECT_NAME + ":operationLock:likeLock:";
    /**
     * 收藏操作的分布式锁
     */
    public static final String PATH_AOPLOCK_FAVORITE = PROJECT_NAME + ":operationLock:favoriteLock:";
    /**
     * 举报操作的分布式锁
     */
    public static final String PATH_AOPLOCK_AGAINST = PROJECT_NAME + ":operationLock:againstLock:";
    /**
     * UPDATE操作的分布式锁
     */
    public static final String PATH_AOPLOCK_UPDATE = PROJECT_NAME + ":operationLock:updateLock:";
    /**
     * INSERT操作的分布式锁
     */
    public static final String PATH_AOPLOCK_INSERT = PROJECT_NAME + ":operationLock:insertLock:";
    /**
     * DELETE操作的分布式锁
     */
    public static final String PATH_AOPLOCK_DELETE = PROJECT_NAME + ":operationLock:deleteLock:";
    /**
     * [key]新通知标志
     */
    public static final String KEY_NOTICE = PROJECT_NAME + ":notice:";
    /**
     * [key]定时任务的分布式锁
     */
    public static final String KEY_SCHEDULE_LOCK = PROJECT_NAME + ":schedule:lock";
    /**
     * [key]定时任务下次生效的时间
     */
    public static final String KEY_SCHEDULE_TIME = PROJECT_NAME + ":schedule:aimTime";
    /**
     * [path]热榜路径（正式）
     */
    public static final String PATH_INDEX_RANKLIST_ACTIVE = PROJECT_NAME + ":index:rankList:active:";
    /**
     * [path]热榜路径（备用）
     */
    public static final String PATH_INDEX_RANKLIST_BUFFER = PROJECT_NAME + ":index:rankList:buffer:";
    
    /**
     * [key]最多人点赞的话题（1周内）
     */
    public static final String KEY_WEEK_MOST_LIKE = "weekMostLike";
    /**
     * 最多人点赞的话题key列表
     */
    public static final List<String> INDEX_LIKE_KEY_LIST = List.of(
            KEY_WEEK_MOST_LIKE
    );
    /**
     * [key]最多人收藏的话题（1周内）
     */
    public static final String KEY_WEEK_MOST_FAVORITE = "weekMostFavorite";
    /**
     * 最多人收藏的话题key列表
     */
    public static final List<String> INDEX_FAVORITE_KEY_LIST = List.of(
            KEY_WEEK_MOST_FAVORITE
    );
    /**
     * 最多人点赞+最多人收藏的话题key列表
     */
    public static final List<String> INDEX_KEY_LIST = Stream.concat(
            INDEX_LIKE_KEY_LIST.stream(),
            INDEX_FAVORITE_KEY_LIST.stream()
    ).toList();
    /**
     * 最多人点赞+收藏的key与其对应的话题列表map
     */
    public static Map<String, List<Topic>> INDEX_KEY_MAP = INDEX_KEY_LIST.stream()
            .collect(Collectors.toMap(
                    Function.identity(),
                    key -> new ArrayList<>()
            ));
    /**
     * [path]热榜话题
     */
    public static final String PATH_TOPIC = "topic:";
    /**
     * [path]热榜话题的新增点赞/收藏数
     */
    public static final String PATH_TOPIC_LIKE_OR_FAVORITE_NUM = "topicLikeOrFavoriteNum:";
    /**
     * [key]热榜数据正在更新标志
     */
    public static final String KEY_UPDATE_SIGN = PROJECT_NAME + ":index:rankList:isUpdating";
    /**
     * [key]布隆过滤器
     */
    public static final String KEY_BLOOMFILTER = PROJECT_NAME + ":bloomFilter:" + "joinping_BF";
    
    public static String getKeyWeekMostLike(boolean toBuffer) {
        if (toBuffer) {
            return PATH_INDEX_RANKLIST_BUFFER + KEY_WEEK_MOST_LIKE;
        } else {
            return PATH_INDEX_RANKLIST_ACTIVE + KEY_WEEK_MOST_LIKE;
        }
    }
    
    public static String getKeyWeekMostFavorite(boolean toBuffer) {
        if (toBuffer) {
            return PATH_INDEX_RANKLIST_BUFFER + KEY_WEEK_MOST_FAVORITE;
        } else {
            return PATH_INDEX_RANKLIST_ACTIVE + KEY_WEEK_MOST_FAVORITE;
        }
    }
    
    public static String getKeyTopic(boolean toBuffer, Long id) {
        if (toBuffer) {
            return PATH_INDEX_RANKLIST_BUFFER + PATH_TOPIC + id;
        } else {
            return PATH_INDEX_RANKLIST_ACTIVE + PATH_TOPIC + id;
        }
    }
    
    public static String getPathTopicLikeOrFavoriteNum(boolean toBuffer) {
        if (toBuffer) {
            return PATH_INDEX_RANKLIST_BUFFER + PATH_TOPIC_LIKE_OR_FAVORITE_NUM;
        } else {
            return PATH_INDEX_RANKLIST_ACTIVE + PATH_TOPIC_LIKE_OR_FAVORITE_NUM;
        }
    }
    
    public static String getKeyTopicLikeOrFavoriteNum(boolean toBuffer, Long id) {
        if (toBuffer) {
            return PATH_INDEX_RANKLIST_BUFFER + PATH_TOPIC_LIKE_OR_FAVORITE_NUM + id;
        } else {
            return PATH_INDEX_RANKLIST_ACTIVE + PATH_TOPIC_LIKE_OR_FAVORITE_NUM + id;
        }
    }
    
    
}
