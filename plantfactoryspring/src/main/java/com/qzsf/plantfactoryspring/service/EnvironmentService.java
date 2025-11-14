package com.qzsf.plantfactoryspring.service;

import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import com.qzsf.plantfactoryspring.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 环境数据管理服务
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;

    /**
     * 保存环境数据
     */
    @Transactional
    public EnvironmentData saveEnvironmentData(EnvironmentData environmentData) {
        // 设置采集时间（如果未设置）
        if (environmentData.getCollectedAt() == null) {
            environmentData.setCollectedAt(LocalDateTime.now());
        }

        // 自动设置单位（基于数据类型）
        if (environmentData.getUnit() == null && environmentData.getDataType() != null) {
            environmentData.setUnit(environmentData.getDataType().getUnit());
        }

        // 检查并更新数据状态
        environmentData.updateStatus();

        EnvironmentData saved = environmentRepository.save(environmentData);
        log.info("保存环境数据成功: 传感器={}, 类型={}, 值={}, 状态={}",
                saved.getSensorId(), saved.getDataType(), saved.getValue(), saved.getStatus());
        return saved;
    }

    /**
     * 批量保存环境数据
     */
    @Transactional
    public List<EnvironmentData> saveEnvironmentDataBatch(List<EnvironmentData> environmentDataList) {
        environmentDataList.forEach(data -> {
            if (data.getCollectedAt() == null) {
                data.setCollectedAt(LocalDateTime.now());
            }
            if (data.getUnit() == null && data.getDataType() != null) {
                data.setUnit(data.getDataType().getUnit());
            }
            data.updateStatus();
        });

        List<EnvironmentData> saved = environmentRepository.saveAll(environmentDataList);
        log.info("批量保存环境数据成功: {} 条", saved.size());
        return saved;
    }

    /**
     * 根据ID查询环境数据
     */
    @Transactional(readOnly = true)
    public Optional<EnvironmentData> findById(Long id) {
        return environmentRepository.findById(id);
    }

    /**
     * 根据传感器ID查询最新数据
     */
    @Transactional(readOnly = true)
    public Optional<EnvironmentData> findLatestBySensorId(String sensorId) {
        return environmentRepository.findFirstBySensorIdAndIsDeletedFalseOrderByCollectedAtDesc(sensorId);
    }

    /**
     * 根据传感器ID和数据类型查询最新数据
     */
    @Transactional(readOnly = true)
    public Optional<EnvironmentData> findLatestBySensorIdAndDataType(String sensorId, EnvironmentData.DataType dataType) {
        return environmentRepository.findFirstBySensorIdAndDataTypeAndIsDeletedFalseOrderByCollectedAtDesc(sensorId, dataType);
    }

    /**
     * 查询指定时间范围内的环境数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime,
                                               int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return environmentRepository.findByCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
                startTime, endTime, pageable);
    }

    /**
     * 查询指定传感器的环境数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> findBySensorId(String sensorId, LocalDateTime startTime, LocalDateTime endTime,
                                               int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return environmentRepository.findBySensorIdAndCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
                sensorId, startTime, endTime, pageable);
    }

    /**
     * 查询指定数据类型的环境数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> findByDataType(EnvironmentData.DataType dataType, LocalDateTime startTime, LocalDateTime endTime,
                                               int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return environmentRepository.findByDataTypeAndCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
                dataType, startTime, endTime, pageable);
    }

    /**
     * 查询异常数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> findAbnormalData(EnvironmentData.DataStatus status, LocalDateTime startTime, LocalDateTime endTime,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return environmentRepository.findByStatusAndIsDeletedFalseAndCollectedAtBetweenOrderByCollectedAtDesc(
                status, startTime, endTime, pageable);
    }

    /**
     * 查询超出阈值的数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> findOutOfRangeData(LocalDateTime startTime, LocalDateTime endTime,
                                                   int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return environmentRepository.findOutOfRangeData(startTime, endTime, pageable);
    }

    /**
     * 获取数据统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDataStatistics(EnvironmentData.DataType dataType,
                                                LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();

        Optional<BigDecimal> avgValue = environmentRepository.findAverageByDataTypeAndTimeRange(
                dataType, startTime, endTime);
        Optional<BigDecimal> maxValue = environmentRepository.findMaxByDataTypeAndTimeRange(
                dataType, startTime, endTime);
        Optional<BigDecimal> minValue = environmentRepository.findMinByDataTypeAndTimeRange(
                dataType, startTime, endTime);
        Long count = environmentRepository.countByDataTypeAndCollectedAtBetweenAndIsDeletedFalse(
                dataType, startTime, endTime);

        statistics.put("dataType", dataType);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);
        statistics.put("average", avgValue.orElse(null));
        statistics.put("maximum", maxValue.orElse(null));
        statistics.put("minimum", minValue.orElse(null));
        statistics.put("count", count);

        return statistics;
    }

    /**
     * 获取所有数据类型的统计信息
     */
    @Transactional(readOnly = true)
    public Map<EnvironmentData.DataType, Map<String, Object>> getAllDataTypeStatistics(
            LocalDateTime startTime, LocalDateTime endTime) {
        Map<EnvironmentData.DataType, Map<String, Object>> allStatistics = new HashMap<>();

        for (EnvironmentData.DataType dataType : EnvironmentData.DataType.values()) {
            allStatistics.put(dataType, getDataStatistics(dataType, startTime, endTime));
        }

        return allStatistics;
    }

    /**
     * 获取最近N小时的图表数据
     */
    @Transactional(readOnly = true)
    public List<EnvironmentData> getChartData(EnvironmentData.DataType dataType, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return environmentRepository.findRecentDataByHours(dataType, since);
    }

    /**
     * 获取传感器列表的最新数据
     */
    @Transactional(readOnly = true)
    public Map<String, EnvironmentData> getLatestDataBySensorIds(List<String> sensorIds) {
        List<EnvironmentData> latestData = environmentRepository.findLatestBySensorIds(sensorIds);
        return latestData.stream()
                .collect(Collectors.toMap(EnvironmentData::getSensorId, data -> data));
    }

    /**
     * 获取所有活跃的传感器ID
     */
    @Transactional(readOnly = true)
    public List<String> getAllActiveSensorIds() {
        return environmentRepository.findAllActiveSensorIds();
    }

    /**
     * 删除旧数据（数据清理）
     */
    @Transactional
    public int deleteOldData(LocalDateTime beforeTime) {
        int deletedCount = environmentRepository.softDeleteOldData(beforeTime);
        log.info("删除旧数据完成: 删除 {} 条 {} 之前的数据", deletedCount, beforeTime);
        return deletedCount;
    }

    /**
     * 根据查询条件搜索环境数据
     */
    @Transactional(readOnly = true)
    public Page<EnvironmentData> searchEnvironmentData(String sensorId, EnvironmentData.DataType dataType,
                                                     EnvironmentData.DataStatus status,
                                                     LocalDateTime startTime, LocalDateTime endTime,
                                                     int page, int size) {
        Specification<EnvironmentData> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 软删除过滤
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // 传感器ID过滤
            if (sensorId != null && !sensorId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("sensorId"), "%" + sensorId + "%"));
            }

            // 数据类型过滤
            if (dataType != null) {
                predicates.add(criteriaBuilder.equal(root.get("dataType"), dataType));
            }

            // 状态过滤
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 时间范围过滤
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("collectedAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("collectedAt"), endTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collectedAt"));
        return environmentRepository.findAll(specification, pageable);
    }

    /**
     * 检查传感器是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsSensor(String sensorId) {
        return environmentRepository.existsBySensorIdAndIsDeletedFalse(sensorId);
    }

    /**
     * 创建模拟数据（用于测试和演示）
     */
    @Transactional
    public List<EnvironmentData> createMockData() {
        List<EnvironmentData> mockData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        // 创建不同类型的传感器数据
        String[] sensorIds = {"TEMP_001", "HUM_001", "LIGHT_001", "CO2_001", "SOIL_001", "PH_001"};

        for (int i = 0; i < 24; i++) { // 创建24小时的数据
            for (int j = 0; j < sensorIds.length; j++) {
                String sensorId = sensorIds[j];
                EnvironmentData.DataType dataType = EnvironmentData.DataType.values()[j];

                EnvironmentData data = EnvironmentData.builder()
                        .sensorId(sensorId)
                        .dataType(dataType)
                        .unit(dataType.getUnit())
                        .collectedAt(now.minusHours(i))
                        .source("SENSOR")
                        .build();

                // 根据数据类型生成不同的随机值
                switch (dataType) {
                    case TEMPERATURE:
                        data.setValue(BigDecimal.valueOf(20 + random.nextDouble() * 10)); // 20-30°C
                        data.setMinThreshold(BigDecimal.valueOf(18));
                        data.setMaxThreshold(BigDecimal.valueOf(32));
                        break;
                    case HUMIDITY:
                        data.setValue(BigDecimal.valueOf(60 + random.nextDouble() * 20)); // 60-80%
                        data.setMinThreshold(BigDecimal.valueOf(50));
                        data.setMaxThreshold(BigDecimal.valueOf(90));
                        break;
                    case LIGHT_INTENSITY:
                        data.setValue(BigDecimal.valueOf(1000 + random.nextDouble() * 9000)); // 1000-10000 lx
                        data.setMinThreshold(BigDecimal.valueOf(500));
                        data.setMaxThreshold(BigDecimal.valueOf(15000));
                        break;
                    case CO2:
                        data.setValue(BigDecimal.valueOf(400 + random.nextDouble() * 600)); // 400-1000 ppm
                        data.setMinThreshold(BigDecimal.valueOf(300));
                        data.setMaxThreshold(BigDecimal.valueOf(1500));
                        break;
                    case SOIL_MOISTURE:
                        data.setValue(BigDecimal.valueOf(40 + random.nextDouble() * 30)); // 40-70%
                        data.setMinThreshold(BigDecimal.valueOf(30));
                        data.setMaxThreshold(BigDecimal.valueOf(80));
                        break;
                    case PH:
                        data.setValue(BigDecimal.valueOf(6.0 + random.nextDouble() * 2.0)); // 6.0-8.0 pH
                        data.setMinThreshold(BigDecimal.valueOf(5.5));
                        data.setMaxThreshold(BigDecimal.valueOf(8.5));
                        break;
                }

                data.updateStatus();
                mockData.add(data);
            }
        }

        return saveEnvironmentDataBatch(mockData);
    }

    /**
     * 更新环境数据的阈值设置
     */
    @Transactional
    public EnvironmentData updateThresholds(Long id, BigDecimal minThreshold, BigDecimal maxThreshold) {
        Optional<EnvironmentData> optionalData = environmentRepository.findById(id);
        if (optionalData.isEmpty()) {
            throw new RuntimeException("环境数据不存在: " + id);
        }

        EnvironmentData data = optionalData.get();
        data.setMinThreshold(minThreshold);
        data.setMaxThreshold(maxThreshold);
        data.updateStatus();

        return environmentRepository.save(data);
    }

    /**
     * 删除环境数据
     */
    @Transactional
    public void deleteEnvironmentData(Long id) {
        Optional<EnvironmentData> optionalData = environmentRepository.findById(id);
        if (optionalData.isPresent()) {
            EnvironmentData data = optionalData.get();
            data.softDelete();
            environmentRepository.save(data);
            log.info("软删除环境数据: ID={}, 传感器={}", id, data.getSensorId());
        }
    }
}