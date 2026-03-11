package com.example.joinping.aop.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.constant.LuaScriptConstants;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.enums.ErrorCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的切面类
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Around("@annotation(distributedOperationLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedOperationLock distributedOperationLock) throws Throwable {
        // 获取键
        String lockValue = Long.toString(StpUtil.getLoginIdAsLong());
        String lockKey = distributedOperationLock.value().getPrefix() + lockValue;
        // 获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey, lockValue, distributedOperationLock.value().getExpire(), TimeUnit.MILLISECONDS
        );
        // 获取锁失败，拦截请求
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new BusinessException(ErrorCodeEnum.REQUEST_FREQUENT_ERROR);
        }
        // 放行请求
        try {
            log.info(String.format("分布式限流锁： %s 获取成功", lockKey));
            return joinPoint.proceed();
        } finally {
            // 使用Lua脚本原子性释放锁
            releaseLockSafely(lockKey, lockValue);
        }
    }
    
    /**
     * 利用lua脚本的原子性防止误删他人的锁
     *
     * @param lockKey
     * @param expectedValue
     */
    private void releaseLockSafely(String lockKey, String expectedValue) {
        try {
            redisTemplate.execute(
                    new DefaultRedisScript<>(LuaScriptConstants.UNLOCK_OPERATIONLOCK_SCRIPT, Long.class),
                    Collections.singletonList(lockKey),
                    expectedValue
            );
        } catch (Exception e) {
            log.error("释放分布式锁失败, lockKey: {}", lockKey, e);
        }
    }
    
}
