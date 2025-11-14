package com.qzsf.plantfactoryspring.entity;

import com.qzsf.plantfactoryspring.entity.base.SoftDeleteEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 环境数据实体类
 * 存储植物工厂的环境监控数据
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
@Table(name = "environment_data",
       indexes = {
           @Index(name = "idx_sensor_id", columnList = "sensor_id"),
           @Index(name = "idx_data_type", columnList = "data_type"),
           @Index(name = "idx_collected_at", columnList = "collected_at"),
           @Index(name = "idx_env_created_at", columnList = "created_at")
       })
public class EnvironmentData extends SoftDeleteEntity {

    /**
     * 传感器ID
     */
    @NotNull(message = "传感器ID不能为空")
    @Column(name = "sensor_id", nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '传感器ID'")
    private String sensorId;

    /**
     * 数据类型
     */
    @NotNull(message = "数据类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, columnDefinition = "ENUM('TEMPERATURE', 'HUMIDITY', 'LIGHT_INTENSITY', 'CO2', 'SOIL_MOISTURE', 'PH') COMMENT '数据类型'")
    private DataType dataType;

    /**
     * 数值
     */
    @NotNull(message = "数值不能为空")
    @DecimalMin(value = "-999.99", message = "数值不能小于-999.99")
    @DecimalMax(value = "999.99", message = "数值不能大于999.99")
    @Column(name = "measurement_value", nullable = false, precision = 6, scale = 2, columnDefinition = "DECIMAL(6,2) COMMENT '测量值'")
    private BigDecimal value;

    /**
     * 单位
     */
    @Column(name = "unit", length = 10, columnDefinition = "VARCHAR(10) COMMENT '单位'")
    private String unit;

    /**
     * 数据采集时间
     */
    @NotNull(message = "采集时间不能为空")
    @Column(name = "collected_at", nullable = false, columnDefinition = "DATETIME COMMENT '数据采集时间'")
    private LocalDateTime collectedAt;

    /**
     * 数据来源
     */
    @Column(name = "source", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'SENSOR' COMMENT '数据来源'")
    @Builder.Default
    private String source = "SENSOR";

    /**
     * 数据状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('NORMAL', 'ABNORMAL', 'WARNING') DEFAULT 'NORMAL' COMMENT '数据状态'")
    @Builder.Default
    private DataStatus status = DataStatus.NORMAL;

    /**
     * 最小阈值
     */
    @Column(name = "min_threshold", precision = 6, scale = 2, columnDefinition = "DECIMAL(6,2) COMMENT '最小阈值'")
    private BigDecimal minThreshold;

    /**
     * 最大阈值
     */
    @Column(name = "max_threshold", precision = 6, scale = 2, columnDefinition = "DECIMAL(6,2) COMMENT '最大阈值'")
    private BigDecimal maxThreshold;

    /**
     * 备注信息
     */
    @Column(name = "remarks", length = 255, columnDefinition = "VARCHAR(255) COMMENT '备注信息'")
    private String remarks;

    /**
     * 数据类型枚举
     */
    public enum DataType {
        TEMPERATURE("TEMPERATURE", "温度", "°C"),
        HUMIDITY("HUMIDITY", "湿度", "%RH"),
        LIGHT_INTENSITY("LIGHT_INTENSITY", "光照强度", "lx"),
        CO2("CO2", "二氧化碳", "ppm"),
        SOIL_MOISTURE("SOIL_MOISTURE", "土壤湿度", "%"),
        PH("PH", "pH值", "pH");

        private final String code;
        private final String description;
        private final String unit;

        DataType(String code, String description, String unit) {
            this.code = code;
            this.description = description;
            this.unit = unit;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getUnit() {
            return unit;
        }

        /**
         * 根据代码获取数据类型
         */
        public static DataType fromCode(String code) {
            for (DataType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown data type code: " + code);
        }
    }

    /**
     * 数据状态枚举
     */
    public enum DataStatus {
        NORMAL("NORMAL", "正常"),
        ABNORMAL("ABNORMAL", "异常"),
        WARNING("WARNING", "警告");

        private final String code;
        private final String description;

        DataStatus(String code, String description) {
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

    /**
     * 检查数据是否超出阈值
     */
    public boolean isOutOfRange() {
        if (minThreshold != null && value.compareTo(minThreshold) < 0) {
            return true;
        }
        if (maxThreshold != null && value.compareTo(maxThreshold) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 更新数据状态（基于阈值）
     */
    public void updateStatus() {
        if (minThreshold != null && value.compareTo(minThreshold) < 0) {
            this.status = DataStatus.WARNING;
        } else if (maxThreshold != null && value.compareTo(maxThreshold) > 0) {
            this.status = DataStatus.WARNING;
        } else {
            this.status = DataStatus.NORMAL;
        }
    }

    /**
     * 获取完整的数据类型描述
     */
    public String getFullDataTypeDescription() {
        return dataType != null ? dataType.getDescription() + " (" + dataType.getUnit() + ")" : null;
    }
}