package com.qzsf.plantfactoryspring.security;

import com.qzsf.plantfactoryspring.service.UserDetailsServiceImpl;
import com.qzsf.plantfactoryspring.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 处理JWT令牌的验证和用户认证
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * JWT令牌前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头名称
     */
    private static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            // 从请求头中获取JWT令牌
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 验证令牌并设置认证信息
                authenticateUser(jwt, request);
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证信息", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌，如果没有找到则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        return null;
    }

    /**
     * 验证JWT令牌并设置用户认证信息
     *
     * @param jwt JWT令牌
     * @param request HTTP请求
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        try {
            // 从令牌中获取用户名
            String username = jwtUtil.getUsernameFromToken(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 验证令牌
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置认证信息到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("用户 {} 认证成功", username);
                } else {
                    log.warn("JWT令牌验证失败: {}", username);
                }
            }
        } catch (Exception ex) {
            log.error("JWT认证过程中发生错误", ex);
        }
    }

    /**
     * 判断是否需要过滤该请求
     * 跳过公开接口的JWT验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 跳过公开接口
        return path.startsWith("/auth/") ||
               path.startsWith("/public/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/favicon.ico");
    }
}