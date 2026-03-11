package com.example.joinping.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式唯一ID生成器（雪花算法改进版）
 * Long结构：2位时间ID + 41位时间戳 + 5位数据中心ID + 3位机器ID + 12位序列号 ——>  高位63[ 时间ID , 时间戳 , 数据中心ID , 机器ID , 序列号 ]低位0
 */
@Slf4j
@Getter
public class DistributedIdGenerator {
    
    /**
     * 起始时间戳（2024-01-01 00:00:00）
     */
    private static final long START_TIMESTAMP = 1704067200000L;
    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;
    
    
    // ============================== 常量配置 ===================================
    /**
     * 时间ID位数
     */
    private static final long TIME_ID_BITS = 2L;
    /**
     * 机器ID位数
     */
    private static final long WORKER_ID_BITS = 3L;
    /**
     * 数据中心ID位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;
    /**
     * 序列号最大值
     */
    private static final long MAX_SEQUENCE = 1L << SEQUENCE_BITS;
    /**
     * 时间ID最大值
     */
    private static final long MAX_TIME_ID = 1L << TIME_ID_BITS;
    /**
     * 机器ID最大值
     */
    private static final long MAX_WORKER_ID = 1L << WORKER_ID_BITS;
    /**
     * 数据中心ID最大值
     */
    private static final long MAX_DATA_CENTER_ID = 1L << DATA_CENTER_ID_BITS;
    /**
     * 序列号左移位数
     */
    private static final long SEQUENCE_SHIFT = 0L;
    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /**
     * 数据中心ID左移位数
     */
    private static final long DATA_CENTER_ID_SHIFT = WORKER_ID_SHIFT + WORKER_ID_BITS;
    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = DATA_CENTER_ID_SHIFT + DATA_CENTER_ID_BITS;
    /**
     * 时间ID左移位数
     */
    private static final long TIME_ID_SHIFT = TIMESTAMP_SHIFT + 41L; // 41位时间戳
    /**
     * 异常计数器
     */
    private static final AtomicLong exceptionCounter = new AtomicLong(0);
    /**
     * 序列号
     */
    private static long sequence = 0L;
    /**
     * 时间ID
     */
    private static long timeId = 0L;

// ============================== 成员变量 ===================================
    /**
     * 机器ID
     */
    private static long workerId = 1L;
    /**
     * 数据中心ID
     */
    private static long dataCenterId = 1L;
    /**
     * 上次生成ID的时间戳
     */
    private static long lastTimestamp = -1L;
    /**
     * 发生时间回拨后最多等待的毫秒数（10秒）
     */
    private static long maxWaitTime = 10000L;
    
    // ============================== 构造方法 ===================================
    
    /**
     * 构造函数(单机下默认配置)
     *
     * @param dataCenterId 数据中心ID (0-31)
     * @param workerId     机器ID (0-31)
     */
    public DistributedIdGenerator() {
    }
    
    /**
     * 构造函数
     *
     * @param dataCenterId 数据中心ID (0-31)
     * @param workerId     机器ID (0-31)
     */
    public DistributedIdGenerator(long dataCenterId, long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("workerId不能大于 %d 或小于 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenterId不能大于 %d 或小于 0", MAX_DATA_CENTER_ID));
        }
        
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }
    
    
    // ============================== 测试方法 ===================================
    public static void main(String[] args) {
        // 创建ID生成器（数据中心ID=1，机器ID=1）
        DistributedIdGenerator idGenerator = new DistributedIdGenerator();
        // 生成10个ID
        for (int i = 0; i < 10; i++) {
            long id = idGenerator.nextId();
            DistributedIdGenerator.IdInfo info = idGenerator.parseId(id);
            System.out.println("生成的ID: " + id + " -> " + info);
        }
    }
    
    /**
     * 生成下一个ID
     */
    public static synchronized long nextId() {
        //获取当前时间戳
        long currentTimestamp = System.currentTimeMillis();
        // 检测时间回拨
        if (currentTimestamp < lastTimestamp) {
            // 发生了时间回拨，进行异常处理
            long offset = lastTimestamp - currentTimestamp;
            // 时间回拨在可接收范围内则等待
            if (offset <= maxWaitTime) {
                try {
                    Thread.sleep(offset + 1);
                    currentTimestamp = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    currentTimestamp = System.currentTimeMillis();
                    while (currentTimestamp <= lastTimestamp) {
                        currentTimestamp = System.currentTimeMillis();
                    }
                }
            } else {
                timeId++;
                log.error(
                        "发生时间回拨现象，当前时间戳为: " + currentTimestamp
                                + " ,旧时间戳为: " + lastTimestamp
                                + " 倒退: " + offset
                                + " 毫秒,新时钟ID为: " + timeId
                );
                // 时间ID用完，出大问题
                if (timeId >= MAX_TIME_ID) {
                    throw new RuntimeException("时钟回拨次数超过上限");
                }
            }
        }
        // 与上次生成ID为同一毫秒内
        if (lastTimestamp == currentTimestamp) {
            sequence++;
            //序列号用完
            if (sequence >= MAX_SEQUENCE) {
                currentTimestamp = waitUntilNextMillis(lastTimestamp);
                sequence = 0L;
            }
        } else {
            // 新的毫秒，重置序列号
            sequence = 0L;
        }
        // 更新上一次生成ID的时间戳
        lastTimestamp = currentTimestamp;
        // 组合生成ID
        return (timeId << TIME_ID_SHIFT)
                | ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | (sequence << SEQUENCE_SHIFT);
    }
    
    
    // ============================== 解析方法 ===================================
    
    /**
     * 循环等待直到下一毫秒
     */
    private static long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    
    // ============================== 监控方法 ===================================
    
    /**
     * 解析ID的各个组成部分
     */
    public IdInfo parseId(long id) {
        long timeId = (id >> TIME_ID_SHIFT) & ~(-1L << TIME_ID_BITS);
        long timestamp = ((id >> TIMESTAMP_SHIFT) & ~(-1L << 41L)) + START_TIMESTAMP;
        long dataCenterId = (id >> DATA_CENTER_ID_SHIFT) & ~(-1L << DATA_CENTER_ID_BITS);
        long workerId = (id >> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS);
        long sequence = (id >> SEQUENCE_SHIFT) & ~(-1L << SEQUENCE_BITS);
        
        return new IdInfo(timestamp, dataCenterId, workerId, timeId, sequence);
    }
    
    /**
     * 获取异常计数
     */
    public long getExceptionCount() {
        return exceptionCounter.get();
    }
    
    /**
     * 重置异常计数
     */
    public void resetExceptionCount() {
        exceptionCounter.set(0);
    }
    
    // ============================== ID信息载体 ===================================
    
    
    /**
     * ID信息承载类
     */
    public static class IdInfo {
        private final long timestamp;
        private final long dataCenterId;
        private final long workerId;
        private final long timeId;
        private final long sequence;
        
        public IdInfo(long timestamp, long dataCenterId, long workerId, long timeId, long sequence) {
            this.timestamp = timestamp;
            this.dataCenterId = dataCenterId;
            this.workerId = workerId;
            this.timeId = timeId;
            this.sequence = sequence;
        }
        
        
        @Override
        public String toString() {
            return String.format("分布式ID信息[时间戳=%d, 数据中心ID=%d, 机器ID=%d, 时间ID=%d, 序列号=%d]",
                    timestamp, dataCenterId, workerId, timeId, sequence);
        }
    }
}