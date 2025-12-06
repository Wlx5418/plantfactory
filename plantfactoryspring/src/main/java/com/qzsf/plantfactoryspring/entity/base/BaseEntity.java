package com.qzsf.plantfactoryspring.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含审计字段：创建时间、更新时间、创建人、更新人
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT AUTO_INCREMENT COMMENT '主键ID'")
    protected Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME COMMENT '创建时间'")
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "DATETIME COMMENT '更新时间'")
    protected LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}