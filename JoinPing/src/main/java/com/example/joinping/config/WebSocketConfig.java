package com.example.joinping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 */
@Configuration
public class WebSocketConfig {
    /**
     * ServerEndpointExporter就是用来扫描所有的@ServerEndpoint注解，
     * 并将这些 WebSocket endpoint 注册到 WebSocket 服务器中。
     * 这样，客户端就可以通过这些 URL 地址来连接到 WebSocket 服务器。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}