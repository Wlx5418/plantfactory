package com.qzsf.plantfactoryspring.dto;

import com.qzsf.plantfactoryspring.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户DTO
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 角色列表
     */
    private Set<RoleDTO> roles;

    /**
     * 权限编码列表
     */
    private Set<String> permissions;

    /**
     * 角色DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDTO {
        private Long id;
        private String roleName;
        private String roleCode;
        private String description;
    }

    /**
     * 从User实体转换为DTO
     */
    public static UserDTO fromEntity(com.qzsf.plantfactoryspring.entity.User user) {
        if (user == null) {
            return null;
        }

        // 转换角色列表
        Set<RoleDTO> roleDTOs = user.getRoles().stream()
                .map(role -> RoleDTO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .description(role.getDescription())
                        .build())
                .collect(Collectors.toSet());

        // 获取权限编码列表
        Set<String> permissionCodes = user.getPermissionCodes();

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().getCode())
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .roles(roleDTOs)
                .permissions(permissionCodes)
                .build();
    }
}