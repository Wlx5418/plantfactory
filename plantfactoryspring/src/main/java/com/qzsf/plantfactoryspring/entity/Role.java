package com.qzsf.plantfactoryspring.entity;

import com.qzsf.plantfactoryspring.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
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
@Table(name = "roles",
       indexes = {
           @Index(name = "idx_role_code", columnList = "role_code"),
           @Index(name = "idx_role_status", columnList = "status")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_role_name", columnNames = {"role_name"}),
           @UniqueConstraint(name = "uk_role_code", columnNames = {"role_code"})
       })
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Column(name = "role_name", nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '角色名称'")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Column(name = "role_code", nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '角色编码'")
    private String roleCode;

    /**
     * 角色描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '角色描述'")
    private String description;

    /**
     * 角色状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态'")
    @Builder.Default
    private RoleStatus status = RoleStatus.ACTIVE;

    /**
     * 角色权限关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 角色状态枚举
     */
    public enum RoleStatus {
        ACTIVE("ACTIVE", "启用"),
        INACTIVE("INACTIVE", "禁用");

        private final String code;
        private final String description;

        RoleStatus(String code, String description) {
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
}