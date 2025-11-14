package com.qzsf.plantfactoryspring.entity;

import com.qzsf.plantfactoryspring.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 权限实体类
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
@Table(name = "permissions",
       indexes = {
           @Index(name = "idx_permission_code", columnList = "code"),
           @Index(name = "idx_resource_type", columnList = "resource_type")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_permission_code", columnNames = {"code"}),
           @UniqueConstraint(name = "uk_permission_name", columnNames = {"name"})
       })
public class Permission extends BaseEntity {

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '权限名称'")
    private String name;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @Column(name = "code", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '权限编码'")
    private String code;

    /**
     * 权限描述
     */
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    @Column(name = "description", length = 500, columnDefinition = "VARCHAR(500) COMMENT '权限描述'")
    private String description;

    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, columnDefinition = "ENUM('MENU', 'BUTTON', 'API') DEFAULT 'API' COMMENT '资源类型'")
    @Builder.Default
    private ResourceType resourceType = ResourceType.API;

    /**
     * 资源路径
     */
    @Size(max = 500, message = "资源路径长度不能超过500个字符")
    @Column(name = "resource_path", length = 500, columnDefinition = "VARCHAR(500) COMMENT '资源路径'")
    private String resourcePath;

    /**
     * 请求方法（API权限使用）
     */
    @Size(max = 20, message = "请求方法长度不能超过20个字符")
    @Column(name = "http_method", length = 20, columnDefinition = "VARCHAR(20) COMMENT 'HTTP请求方法'")
    private String httpMethod;

    /**
     * 父权限ID
     */
    @Column(name = "parent_id", columnDefinition = "BIGINT COMMENT '父权限ID'")
    private Long parentId;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0 COMMENT '排序顺序'")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        MENU("MENU", "菜单"),
        BUTTON("BUTTON", "按钮"),
        API("API", "接口");

        private final String code;
        private final String description;

        ResourceType(String code, String description) {
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