package com.qzsf.plantfactoryspring.dto;

import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 环境数据DTO
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDataDTO {

    /**
     * 数据ID
     */
    private Long id;

    /**
     * 传感器ID
     */
    private String sensorId;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数值
     */
    private BigDecimal value;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数据采集时间
     */
    private LocalDateTime collectedAt;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 数据状态
     */
    private String status;

    /**
     * 最小阈值
     */
    private BigDecimal minThreshold;

    /**
     * 最大阈值
     */
    private BigDecimal maxThreshold;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // Excel导入相关字段
    /**
     * 行索引（Excel导入时使用）
     */
    private Integer rowIndex;

    /**
     * 区域ID（Excel导入时使用）
     */
    private Long areaId;

    /**
     * 数据时间（Excel导入时使用）
     */
    private LocalDateTime dataTime;

    /**
     * 温度值（Excel导入时使用）
     */
    private Double temperature;

    /**
     * 湿度值（Excel导入时使用）
     */
    private Double humidity;

    /**
     * 光照强度（Excel导入时使用）
     */
    private Double lightIntensity;

    /**
     * CO2浓度（Excel导入时使用）
     */
    private Double co2Level;

    /**
     * pH值（Excel导入时使用）
     */
    private Double phValue;

    /**
     * EC值（Excel导入时使用）
     */
    private Double ecValue;

    /**
     * 土壤湿度（Excel导入时使用）
     */
    private Double soilMoisture;

    /**
     * 操作员ID（Excel导入时使用）
     */
    private Long operatorId;

    /**
     * 数据来源（Excel导入时使用）
     */
    private String dataSource;

    /**
     * 从EnvironmentData实体转换为DTO
     */
    public static EnvironmentDataDTO fromEntity(EnvironmentData environmentData) {
        if (environmentData == null) {
            return null;
        }

        return EnvironmentDataDTO.builder()
                .id(environmentData.getId())
                .sensorId(environmentData.getSensorId())
                .dataType(environmentData.getDataType() != null ? environmentData.getDataType().getCode() : null)
                .value(environmentData.getValue())
                .unit(environmentData.getUnit())
                .collectedAt(environmentData.getCollectedAt())
                .source(environmentData.getSource())
                .status(environmentData.getStatus() != null ? environmentData.getStatus().getCode() : null)
                .minThreshold(environmentData.getMinThreshold())
                .maxThreshold(environmentData.getMaxThreshold())
                .remarks(environmentData.getRemarks())
                .createdAt(environmentData.getCreatedAt())
                .updatedAt(environmentData.getUpdatedAt())
                .build();
    }

    /**
     * 转换为EnvironmentData实体
     */
    public EnvironmentData toEntity() {
        EnvironmentData.DataType dataTypeEnum = null;
        if (dataType != null) {
            try {
                dataTypeEnum = EnvironmentData.DataType.fromCode(dataType);
            } catch (IllegalArgumentException e) {
                // 如果无法识别的数据类型，保持为null
            }
        }

        EnvironmentData.DataStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = EnvironmentData.DataStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // 如果无法识别的状态，使用默认值
                statusEnum = EnvironmentData.DataStatus.NORMAL;
            }
        }

        EnvironmentData.EnvironmentDataBuilder builder = EnvironmentData.builder()
                .sensorId(sensorId)
                .dataType(dataTypeEnum)
                .value(value)
                .unit(unit)
                .collectedAt(collectedAt)
                .source(source != null ? source : "SENSOR")
                .status(statusEnum != null ? statusEnum : EnvironmentData.DataStatus.NORMAL)
                .minThreshold(minThreshold)
                .maxThreshold(maxThreshold)
                .remarks(remarks);

        // 如果有ID，通过反射设置（因为ID继承自父类）
        if (id != null) {
            try {
                EnvironmentData entity = builder.build();
                entity.setId(id);
                return entity;
            } catch (Exception e) {
                // 如果设置ID失败，忽略错误继续
            }
        }

        return builder.build();
    }

    /**
     * 数据类型枚举DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataTypeDTO {
        private String code;
        private String description;
        private String unit;
    }

    /**
     * 数据状态枚举DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStatusDTO {
        private String code;
        private String description;
    }
}