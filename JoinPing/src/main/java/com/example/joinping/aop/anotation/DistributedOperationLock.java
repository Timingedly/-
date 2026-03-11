package com.example.joinping.aop.anotation;

import com.example.joinping.enums.OperationLockTypeEnum;

import java.lang.annotation.*;

/**
 * 分布式限流锁
 * （不能防止并发问题，但能防止瞬时内大量恶意请求，如Jmeter模拟百万请求。）
 * （只有第一个请求能正常执行，锁释放前其他请求都被拦截）
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedOperationLock {
    OperationLockTypeEnum value();
    
}
