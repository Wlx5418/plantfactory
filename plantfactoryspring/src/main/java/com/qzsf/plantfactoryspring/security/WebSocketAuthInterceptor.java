package com.qzsf.plantfactoryspring.security;

import com.qzsf.plantfactoryspring.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * WebSocket认证拦截器
 * 在WebSocket连接建立时验证JWT令牌
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 在连接建立时验证JWT令牌
            List<String> authorization = accessor.getNativeHeader("Authorization");

            if (authorization == null || authorization.isEmpty()) {
                log.warn("WebSocket连接缺少Authorization头");
                throw new IllegalArgumentException("缺少认证令牌");
            }

            String token = authorization.get(0);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                log.warn("WebSocket连接Authorization头格式错误");
                throw new IllegalArgumentException("认证令牌格式错误");
            }

            try {
                // 验证JWT令牌
                if (!jwtUtil.validateToken(token)) {
                    log.warn("WebSocket连接JWT令牌无效");
                    throw new IllegalArgumentException("无效的认证令牌");
                }

                // 获取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 设置用户认证信息
                accessor.setUser(authentication);

                // 设置到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("WebSocket认证成功: {}", username);

            } catch (Exception e) {
                log.error("WebSocket认证失败: {}", e.getMessage());
                throw new IllegalArgumentException("WebSocket认证失败: " + e.getMessage());
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 连接断开时清理安全上下文
            SecurityContextHolder.clearContext();
            log.info("WebSocket连接断开，已清理安全上下文");
        }
    }
}