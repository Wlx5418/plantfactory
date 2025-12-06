package com.qzsf.plantfactoryspring.security;

import com.qzsf.plantfactoryspring.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手JWT认证拦截器
 * 在WebSocket握手阶段验证JWT令牌，解决最大安全漏洞
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
public class WebSocketJwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketJwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();

                // 优先从Authorization Header获取JWT
                String token = extractTokenFromHeader(httpRequest);

                // 如果Header中没有，尝试从查询参数获取
                if (token == null) {
                    token = httpRequest.getParameter("token");
                }

                // 验证JWT令牌
                if (token != null && jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);

                    // 将用户信息存入WebSocket Session属性
                    attributes.put("username", username);
                    attributes.put("token", token);

                    log.info("WebSocket握手认证成功: {}", username);
                    return true;
                } else {
                    log.warn("WebSocket握手认证失败: 缺少或无效的JWT令牌");

                    // 设置401响应
                    response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("WebSocket握手认证异常", e);
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }

        log.warn("WebSocket握手认证失败: 无效的请求类型");
        response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

        if (exception != null) {
            log.error("WebSocket握手后处理异常", exception);
        } else {
            log.debug("WebSocket握手完成");
        }
    }

    /**
     * 从HTTP Header中提取JWT令牌
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}