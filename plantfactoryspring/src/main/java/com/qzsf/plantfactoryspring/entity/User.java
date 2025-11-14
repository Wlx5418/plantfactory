package com.qzsf.plantfactoryspring.entity;

import com.qzsf.plantfactoryspring.entity.base.SoftDeleteEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体类
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_email", columnList = "email"),
           @Index(name = "idx_user_status", columnList = "status"),
           @Index(name = "idx_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
           @UniqueConstraint(name = "uk_email", columnNames = {"email"})
       })
public class User extends SoftDeleteEntity implements UserDetails {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '用户名'")
    private String username;

    /**
     * 密码（已加密）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Column(name = "password", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '密码(加密)'")
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Column(name = "email", length = 100, columnDefinition = "VARCHAR(100) COMMENT '邮箱'")
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20, columnDefinition = "VARCHAR(20) COMMENT '手机号'")
    private String phone;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 50, columnDefinition = "VARCHAR(50) COMMENT '真实姓名'")
    private String realName;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 255, columnDefinition = "VARCHAR(255) COMMENT '头像URL'")
    private String avatarUrl;

    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE' COMMENT '用户状态'")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time", columnDefinition = "DATETIME COMMENT '最后登录时间'")
    private LocalDateTime lastLoginTime;

    /**
     * 用户角色关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new java.util.HashSet<>();

    /**
     * 用户枚举状态
     */
    public enum UserStatus {
        ACTIVE("ACTIVE", "正常"),
        INACTIVE("INACTIVE", "未激活"),
        LOCKED("LOCKED", "已锁定");

        private final String code;
        private final String description;

        UserStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    // Spring Security UserDetails 接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !UserStatus.LOCKED.equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status) && !isDeleted;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 获取角色编码列表
     */
    public Set<String> getRoleCodes() {
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());
    }

    /**
     * 获取权限编码列表
     */
    public Set<String> getPermissionCodes() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * 检查是否拥有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode));
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }
}