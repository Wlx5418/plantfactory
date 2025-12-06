package com.qzsf.plantfactoryspring.config;

import com.qzsf.plantfactoryspring.security.WebSocketJwtHandshakeInterceptor;
import com.qzsf.plantfactoryspring.utils.JwtUtil;
import com.qzsf.plantfactoryspring.websocket.EnvironmentWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket安全配置 - 握手阶段JWT认证
 * 解决最大安全漏洞：防止未授权访问实时数据
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Configuration
@EnableWebSocket
public class WebSocketHandshakeConfig implements WebSocketConfigurer {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    public WebSocketHandshakeConfig(ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(environmentWebSocketHandler(), "/ws/environment")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketJwtHandshakeInterceptor());
    }

    @Bean
    public EnvironmentWebSocketHandler environmentWebSocketHandler() {
        return new EnvironmentWebSocketHandler(objectMapper);
    }

    @Bean
    public WebSocketJwtHandshakeInterceptor webSocketJwtHandshakeInterceptor() {
        return new WebSocketJwtHandshakeInterceptor(jwtUtil);
    }
}