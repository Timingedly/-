package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.joinping.constant.RedisConstants.*;

/**
 * 分布式限流锁的类型枚举
 */
@Getter
@AllArgsConstructor
public enum OperationLockTypeEnum {
    /**
     * 收藏操作限流锁
     */
    FavoriteLock(PATH_AOPLOCK_FAVORITE, 2000),
    /**
     * 点赞操作限流锁
     */
    LikeLock(PATH_AOPLOCK_LIKE, 2000),
    /**
     * 举报操作限流锁
     */
    AgainstLock(PATH_AOPLOCK_AGAINST, 5000),
    /**
     * UPDATE操作限流锁
     */
    UpdateLock(PATH_AOPLOCK_UPDATE, 10000),
    /**
     * INSERT操作限流锁
     */
    InsertLock(PATH_AOPLOCK_INSERT, 10000),
    /**
     * DELETE操作限流锁
     */
    DeleteLock(PATH_AOPLOCK_DELETE, 5000);
    
    /**
     * 锁的key前缀
     */
    private final String prefix;
    /**
     * 锁过期时间（毫秒）
     */
    private final int expire;
    
}
