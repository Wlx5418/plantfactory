package com.qzsf.plantfactoryspring.dto.auth;

import com.qzsf.plantfactoryspring.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 登录响应DTO
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserDTO userInfo;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;
}