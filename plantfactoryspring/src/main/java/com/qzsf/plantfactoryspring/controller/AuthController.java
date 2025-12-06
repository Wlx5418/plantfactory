package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.dto.auth.LoginRequest;
import com.qzsf.plantfactoryspring.dto.auth.LoginResponse;
import com.qzsf.plantfactoryspring.service.AuthService;
import com.qzsf.plantfactoryspring.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出、令牌刷新等认证相关操作
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

  
    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        try {
            log.info("用户登录请求: {}", loginRequest.getUsername());

            LoginResponse response = authService.login(loginRequest);

            log.info("用户登录成功: {}", loginRequest.getUsername());

            return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                    .code(200)
                    .message("登录成功")
                    .data(response)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("用户登录失败: {} - {}", loginRequest.getUsername(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<LoginResponse>builder()
                            .code(401)
                            .message("用户名或密码错误")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    
    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "unknown";

            log.info("用户登出: {}", username);

            // 执行登出逻辑
            authService.logout(username);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("退出登录成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());

        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("退出登录成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());
        }
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshTokenRequest 刷新令牌请求
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();

            // 验证刷新令牌
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new IllegalArgumentException("刷新令牌无效");
            }

            // 生成新的访问令牌
            String newAccessToken = authService.refreshToken(refreshToken);

            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("tokenType", "Bearer");

            return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                    .code(200)
                    .message("令牌刷新成功")
                    .data(tokenData)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());

        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, String>>builder()
                            .code(401)
                            .message("刷新令牌无效")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 验证令牌有效性
     *
     * @param tokenRequest 令牌验证请求
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @Valid @RequestBody ValidateTokenRequest tokenRequest) {

        try {
            String token = tokenRequest.getToken();
            boolean isValid = authService.validateToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);

            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                result.put("username", username);
                result.put("expiresIn", jwtUtil.getTokenRemainingTime(token) / 1000); // 转换为秒
            }

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("令牌验证完成")
                    .data(result)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());

        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("令牌验证完成")
                    .data(result)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());
        }
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .code(401)
                                .message("用户未认证")
                                .timestamp(Instant.now())
                                .requestId(generateRequestId())
                                .build());
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", authentication.getName());
            userInfo.put("authorities", authentication.getAuthorities());

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("获取用户信息成功")
                    .data(userInfo)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .build());

        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .code(500)
                            .message("获取用户信息失败")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis();
    }

    // 内部请求DTO类

    /**
     * 刷新令牌请求
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /**
     * 令牌验证请求
     */
    public static class ValidateTokenRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * 统一API响应格式
     */
    public static class ApiResponse<T> {
        private Integer code;
        private String message;
        private T data;
        private Instant timestamp;
        private String requestId;
        private Long executionTime;

        // Builder pattern
        public static class Builder<T> {
            private final ApiResponse<T> response = new ApiResponse<>();

            public Builder<T> code(Integer code) {
                response.code = code;
                return this;
            }

            public Builder<T> message(String message) {
                response.message = message;
                return this;
            }

            public Builder<T> data(T data) {
                response.data = data;
                return this;
            }

            public Builder<T> timestamp(Instant timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public Builder<T> requestId(String requestId) {
                response.requestId = requestId;
                return this;
            }

            public Builder<T> executionTime(Long executionTime) {
                response.executionTime = executionTime;
                return this;
            }

            public ApiResponse<T> build() {
                return response;
            }
        }

        public static <T> Builder<T> builder() {
            return new Builder<>();
        }

        // Getters
        public Integer getCode() { return code; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public Instant getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
        public Long getExecutionTime() { return executionTime; }
    }
}