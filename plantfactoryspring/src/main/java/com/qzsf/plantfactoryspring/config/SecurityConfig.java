package com.qzsf.plantfactoryspring.config;

import com.qzsf.plantfactoryspring.security.JwtAuthenticationFilter;
import com.qzsf.plantfactoryspring.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 是否允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用CSRF，因为我们使用JWT
                .csrf(AbstractHttpConfigurer::disable)

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理为无状态
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置认证提供者
                .authenticationProvider(authenticationProvider())

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                    // 公开接口
                    .requestMatchers(
                        AntPathRequestMatcher.antMatcher("/auth/**"),
                        AntPathRequestMatcher.antMatcher("/public/**"),
                        AntPathRequestMatcher.antMatcher("/actuator/health"),
                        AntPathRequestMatcher.antMatcher("/actuator/info"),
                        AntPathRequestMatcher.antMatcher("/h2-console/**"),
                        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                        AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                        AntPathRequestMatcher.antMatcher("/webjars/**"),
                        AntPathRequestMatcher.antMatcher("/swagger-resources/**"),
                        AntPathRequestMatcher.antMatcher("/configuration/**"),
                        AntPathRequestMatcher.antMatcher("/favicon.ico"),
                        AntPathRequestMatcher.antMatcher("/error"),
                        // 临时开放环境数据测试接口
                        AntPathRequestMatcher.antMatcher("/v1/environment/data/mock"),
                        AntPathRequestMatcher.antMatcher("/v1/environment/sensors"),
                        AntPathRequestMatcher.antMatcher("/v1/environment/data/range")
                    ).permitAll()

                    // WebSocket连接需要JWT认证（在握手阶段验证）
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/ws/**")).authenticated()

                    // 管理员接口
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")

                    // 管理员和操作员接口
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/operator/**")).hasAnyRole("ADMIN", "OPERATOR")

                    // 观察员及以上权限接口
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/observer/**")).hasAnyRole("ADMIN", "OPERATOR", "OBSERVER")

                    // 其他所有请求都需要认证
                    .anyRequest().authenticated()
                )

                // 配置异常处理
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("""
                            {
                                "code": 401,
                                "message": "未授权访问",
                                "error": {
                                    "type": "AUTH_ERROR",
                                    "code": "UNAUTHORIZED",
                                    "details": "请先登录后再访问此接口"
                                },
                                "timestamp": "%s"
                            }
                            """.formatted(java.time.Instant.now()));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("""
                            {
                                "code": 403,
                                "message": "权限不足",
                                "error": {
                                    "type": "PERMISSION_ERROR",
                                    "code": "ACCESS_DENIED",
                                    "details": "您没有权限访问此接口"
                                },
                                "timestamp": "%s"
                            }
                            """.formatted(java.time.Instant.now()));
                    })
                )

                .build();
    }
}