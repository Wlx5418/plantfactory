package com.qzsf.plantfactoryspring.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 软删除实体基类
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at", columnDefinition = "DATETIME COMMENT '删除时间'")
    protected LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否删除'")
    protected Boolean isDeleted = false;

    /**
     * 软删除
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * 恢复删除
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }
}