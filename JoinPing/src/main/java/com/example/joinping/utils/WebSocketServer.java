package com.example.joinping.utils;

import cn.hutool.json.JSONUtil;
import com.example.joinping.entity.pojo.Notice;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket工具类
 */
@Component
@ServerEndpoint("/websocket/{userId}")
@Slf4j
public class WebSocketServer {
    
    // 心跳配置常量
    private static final long HEARTBEAT_INTERVAL = 30000; // 30秒发送一次心跳
    private static final long HEARTBEAT_TIMEOUT = 60000;  // 60秒无响应认为连接超时
    private static final String HEARTBEAT_MESSAGE = "{\"type\":\"ping\"}";
    private static final String PONG_MESSAGE = "{\"type\":\"pong\"}";
    private static final CopyOnWriteArraySet<WebSocketServer> webSockets = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<Long, Session> sessionPool = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
    private static RedisUtils redisUtils;
    // 心跳调度器
    private static ScheduledExecutorService heartbeatScheduler;
    private static ScheduledExecutorService timeoutChecker;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean isActive = new AtomicBoolean(true);
    private Session session;
    private Long userId;
    private long lastHeartbeatTime;
    
    /**
     * 广播消息（发送对象）- 改为静态方法以便其他地方调用
     */
    public static void sendAllMessage(Notice message) {
        if (webSockets.isEmpty()) {
            log.info("【WebSocket消息】没有活跃连接，跳过广播消息");
            return;
        }
        
        String jsonMessage = JSONUtil.toJsonStr(message);
        log.info("【WebSocket消息】广播消息，目标连接数：{}，消息内容：{}", webSockets.size(), jsonMessage);
        
        int successCount = 0;
        int failCount = 0;
        
        for (WebSocketServer webSocket : webSockets) {
            try {
                if (webSocket.session != null && webSocket.session.isOpen() && webSocket.isActive.get()) {
                    webSocket.session.getAsyncRemote().sendText(jsonMessage);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("【WebSocket消息】广播消息发送失败，用户ID：{}", webSocket.userId, e);
            }
        }
        
        log.info("【WebSocket消息】广播完成，成功：{}，失败：{}", successCount, failCount);
    }
    
    /**
     * 单点消息（发送对象）- 改为静态方法
     */
    public static void sendOneMessage(Long userId, Notice message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = JSONUtil.toJsonStr(message);
                log.info("【WebSocket消息】单点消息，目标用户：{}，消息内容：{}", userId, jsonMessage);
                session.getAsyncRemote().sendText(jsonMessage);
                
                // 更新心跳时间（消息发送成功也视为活跃）
                lastHeartbeatMap.put(userId, System.currentTimeMillis());
            } catch (Exception e) {
                log.error("【WebSocket消息】单点消息发送失败，用户ID：{}", userId, e);
            }
        } else {
            log.error("【WebSocket消息】用户未连接，无法发送单点消息，用户ID：{}", userId);
        }
    }
    
    /**
     * 单点消息（发送文本）- 改为静态方法
     */
    public static void sendOneText(Long userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
                log.info("【WebSocket消息】单点文本消息发送成功，用户ID：{}", userId);
                
                // 更新心跳时间（消息发送成功也视为活跃）
                lastHeartbeatMap.put(userId, System.currentTimeMillis());
            } catch (Exception e) {
                log.error("【WebSocket消息】单点文本消息发送失败，用户ID：{}", userId, e);
            }
        } else {
            log.error("【WebSocket消息】用户未连接，无法发送单点文本消息，用户ID：{}", userId);
        }
    }
    
    /**
     * 多点消息（发送对象）- 改为静态方法
     */
    public static void sendMoreMessage(Long[] userIds, Notice message) {
        String jsonMessage = JSONUtil.toJsonStr(message);
        log.info("【WebSocket消息】多点消息，目标用户数：{}，消息内容：{}", userIds.length, jsonMessage);
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long userId : userIds) {
            Session session = sessionPool.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(jsonMessage);
                    successCount++;
                    
                    // 更新心跳时间（消息发送成功也视为活跃）
                    lastHeartbeatMap.put(userId, System.currentTimeMillis());
                } catch (Exception e) {
                    failCount++;
                    log.error("【WebSocket消息】多点消息发送失败，用户ID：{}", userId, e);
                }
            } else {
                failCount++;
                log.error("【WebSocket消息】用户未连接，跳过发送，用户ID：{}", userId);
            }
        }
        
        log.info("【WebSocket消息】多点消息发送完成，成功：{}，失败：{}", successCount, failCount);
    }
    
    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return webSockets.size();
    }
    
    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return sessionPool.containsKey(userId);
    }
    
    /**
     * 获取所有在线用户ID
     */
    public static Set<Long> getOnlineUsers() {
        return new HashSet<>(sessionPool.keySet());
    }
    
    /**
     * 获取心跳统计信息（用于监控）
     */
    public static String getHeartbeatStats() {
        return String.format("总连接数: %d, 心跳记录数: %d", webSockets.size(), lastHeartbeatMap.size());
    }
    
    @Resource
    public void setRedisUtils(RedisUtils redisUtils) {
        WebSocketServer.redisUtils = redisUtils;
        log.info("RedisUtils在WebSocketServer中初始化完成");
    }
    
    /**
     * 初始化心跳机制
     */
    @PostConstruct
    public void init() {
        if (heartbeatScheduler == null) {
            heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "websocket-heartbeat");
                t.setDaemon(true);
                return t;
            });
            
            timeoutChecker = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "websocket-timeout-checker");
                t.setDaemon(true);
                return t;
            });
            
            // 启动心跳发送任务
            heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, 10, HEARTBEAT_INTERVAL / 1000, TimeUnit.SECONDS);
            
            // 启动超时检查任务
            timeoutChecker.scheduleAtFixedRate(this::checkTimeouts, 30, 30, TimeUnit.SECONDS);
            
            log.info("WebSocket心跳机制初始化完成，心跳间隔: {}ms, 超时时间: {}ms", HEARTBEAT_INTERVAL, HEARTBEAT_TIMEOUT);
        }
    }
    
    /**
     * 清理资源
     */
    @PreDestroy
    public void destroy() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
            heartbeatScheduler = null;
        }
        if (timeoutChecker != null) {
            timeoutChecker.shutdown();
            timeoutChecker = null;
        }
        log.info("WebSocket心跳机制已关闭");
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        try {
            // 添加更详细的null检查和日志
            if (redisUtils == null) {
                log.error("RedisUtils未初始化，连接可能无法正常工作");
                // 可以选择抛出异常或者继续建立连接但不执行Redis操作
            } else {
                try {
                    if (redisUtils.userIsAlive(userId)) {
                        //用户曾经注册过且还维持着在线，说明此时是多开页面
                        //关闭旧连接，放行当前连接
                        closeOldConnect(userId);
                    } else {
                        redisUtils.insertOrDeleteNotice(false, userId);
                    }
                } catch (Exception e) {
                    log.error("onOpen中Redis操作失败，但连接将继续。用户ID：{}", userId, e);
                }
            }
            
            this.session = session;
            this.userId = userId;
            this.lastHeartbeatTime = System.currentTimeMillis();
            this.isActive.set(true);
            
            webSockets.add(this);
            sessionPool.put(userId, session);
            lastHeartbeatMap.put(userId, this.lastHeartbeatTime);
            
            log.info("【WebSocket消息】有新连接建立，用户ID：{}，当前连接总数：{}", userId, webSockets.size());
        } catch (Exception e) {
            log.error("【WebSocket消息】连接建立失败，用户ID：{}", userId, e);
            // 确保在异常情况下也清理资源
            cleanupOnError();
            throw new RuntimeException("WebSocket连接失败", e);
        }
    }
    
    @OnClose
    public void onClose() {
        try {
            // 在移除之前保存userId，因为this.userId可能在移除后变为null
            Long closingUserId = this.userId;
            
            // 标记为不活跃
            this.isActive.set(false);
            
            if (redisUtils != null) {
                try {
                    redisUtils.insertOrDeleteNotice(true, userId);
                } catch (Exception e) {
                    log.error("onClose中Redis操作失败，用户：{}", closingUserId, e);
                }
            }
            
            // 原子性操作：同时从所有集合中移除
            webSockets.remove(this);
            if (closingUserId != null) {
                sessionPool.remove(closingUserId);
                lastHeartbeatMap.remove(closingUserId);
            }
            
            log.info("【WebSocket消息】连接断开，用户ID：{}，当前连接总数：{}",
                    closingUserId != null ? closingUserId : "未知",
                    webSockets.size());
        } catch (Exception e) {
            log.error("【WebSocket消息】连接断开异常，用户ID：{}", userId, e);
        } finally {
            // 确保资源清理
            this.session = null;
            this.userId = null;
        }
    }
    
    @OnMessage
    public void onMessage(String message) {
        log.info("【WebSocket消息】收到客户端消息：{}", message);
        
        // 处理心跳消息
        if (isHeartbeatMessage(message)) {
            handleHeartbeatMessage(message);
            return;
        }
        
        // 更新最后心跳时间（任何消息都视为活跃）
        updateLastHeartbeatTime();
        
        // 其他业务消息处理可以在这里添加
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket消息】连接错误，用户ID：{}，错误原因：{}", userId, error.getMessage(), error);
        cleanupOnError();
    }
    
    /**
     * 判断是否为心跳消息
     */
    private boolean isHeartbeatMessage(String message) {
        return message != null &&
                (message.contains("\"type\":\"ping\"") ||
                        message.contains("\"type\":\"pong\"") ||
                        message.equals("ping") ||
                        message.equals("pong"));
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(String message) {
        if (message.contains("ping") || message.equals("ping")) {
            // 收到ping，回复pong
            sendPong();
        } else if (message.contains("pong") || message.equals("pong")) {
            // 收到pong，更新心跳时间
            updateLastHeartbeatTime();
            log.debug("【WebSocket心跳】收到pong响应，用户ID：{}", userId);
        }
    }
    
    /**
     * 发送pong响应
     */
    private void sendPong() {
        if (session != null && session.isOpen() && isActive.get()) {
            try {
                session.getAsyncRemote().sendText(PONG_MESSAGE);
                updateLastHeartbeatTime();
                log.debug("【WebSocket心跳】发送pong响应，用户ID：{}", userId);
            } catch (Exception e) {
                log.error("【WebSocket心跳】发送pong失败，用户ID：{}", userId, e);
                isActive.set(false);
            }
        }
    }
    
    /**
     * 发送ping心跳
     */
    private void sendPing() {
        if (session != null && session.isOpen() && isActive.get()) {
            try {
                session.getAsyncRemote().sendText(HEARTBEAT_MESSAGE);
                log.debug("【WebSocket心跳】发送ping心跳，用户ID：{}", userId);
            } catch (Exception e) {
                log.error("【WebSocket心跳】发送ping失败，用户ID：{}", userId, e);
                isActive.set(false);
            }
        }
    }
    
    /**
     * 更新最后心跳时间
     */
    private void updateLastHeartbeatTime() {
        this.lastHeartbeatTime = System.currentTimeMillis();
        if (userId != null) {
            lastHeartbeatMap.put(userId, this.lastHeartbeatTime);
        }
    }
    
    /**
     * 发送心跳给所有连接
     */
    private void sendHeartbeats() {
        try {
            for (WebSocketServer webSocket : webSockets) {
                if (webSocket.isActive.get() && webSocket.session != null && webSocket.session.isOpen()) {
                    webSocket.sendPing();
                }
            }
        } catch (Exception e) {
            log.error("【WebSocket心跳】发送心跳任务执行异常", e);
        }
    }
    
    /**
     * 检查超时连接
     */
    private void checkTimeouts() {
        try {
            long currentTime = System.currentTimeMillis();
            Set<WebSocketServer> timeouts = new HashSet<>();
            
            for (WebSocketServer webSocket : webSockets) {
                if (webSocket.userId == null || !webSocket.isActive.get()) {
                    continue;
                }
                
                Long lastHeartbeat = lastHeartbeatMap.get(webSocket.userId);
                if (lastHeartbeat != null && (currentTime - lastHeartbeat) > HEARTBEAT_TIMEOUT) {
                    log.warn("【WebSocket心跳】连接超时，用户ID：{}，最后活跃时间：{}",
                            webSocket.userId, lastHeartbeat);
                    timeouts.add(webSocket);
                }
            }
            
            // 关闭超时连接
            for (WebSocketServer timeoutSocket : timeouts) {
                try {
                    if (timeoutSocket.session != null && timeoutSocket.session.isOpen()) {
                        timeoutSocket.session.close(new CloseReason(
                                CloseReason.CloseCodes.NORMAL_CLOSURE,
                                "心跳超时"
                        ));
                    }
                } catch (IOException e) {
                    log.error("【WebSocket心跳】关闭超时连接失败，用户ID：{}", timeoutSocket.userId, e);
                } finally {
                    timeoutSocket.isActive.set(false);
                    if (timeoutSocket.userId != null) {
                        lastHeartbeatMap.remove(timeoutSocket.userId);
                    }
                }
            }
            
            if (!timeouts.isEmpty()) {
                log.info("【WebSocket心跳】清理了{}个超时连接", timeouts.size());
            }
        } catch (Exception e) {
            log.error("【WebSocket心跳】超时检查任务执行异常", e);
        }
    }
    
    /**
     * 清理错误时的资源
     */
    private void cleanupOnError() {
        try {
            webSockets.remove(this);
            if (this.userId != null) {
                sessionPool.remove(this.userId);
                lastHeartbeatMap.remove(this.userId);
            }
            this.isActive.set(false);
        } catch (Exception e) {
            log.error("清理过程中发生错误：{}", e.getMessage());
        }
    }
    
    /**
     * 关闭当前用户的旧连接
     *
     * @param userId
     */
    private void closeOldConnect(Long userId) {
        Session oldSession = sessionPool.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                log.info("【WebSocket消息】关闭旧连接，用户ID：{}", userId);
                oldSession.close();
            } catch (IOException e) {
                log.error("关闭旧连接失败，用户ID：{}", userId, e);
            }
        }
    }
}