package com.qzsf.plantfactoryspring.repository;

import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 环境数据访问接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Repository
public interface EnvironmentRepository extends JpaRepository<EnvironmentData, Long>, JpaSpecificationExecutor<EnvironmentData> {

    /**
     * 根据传感器ID查询最新数据
     */
    Optional<EnvironmentData> findFirstBySensorIdAndIsDeletedFalseOrderByCollectedAtDesc(String sensorId);

    /**
     * 根据传感器ID和数据类型查询最新数据
     */
    Optional<EnvironmentData> findFirstBySensorIdAndDataTypeAndIsDeletedFalseOrderByCollectedAtDesc(
            String sensorId, EnvironmentData.DataType dataType);

    /**
     * 查询指定时间范围内的数据
     */
    Page<EnvironmentData> findByCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询指定传感器和时间范围内的数据
     */
    Page<EnvironmentData> findBySensorIdAndCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
            String sensorId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询指定数据类型和时间范围内的数据
     */
    Page<EnvironmentData> findByDataTypeAndCollectedAtBetweenAndIsDeletedFalseOrderByCollectedAtDesc(
            EnvironmentData.DataType dataType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询异常数据
     */
    Page<EnvironmentData> findByStatusAndIsDeletedFalseAndCollectedAtBetweenOrderByCollectedAtDesc(
            EnvironmentData.DataStatus status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询超出阈值的数据
     */
    @Query("SELECT e FROM EnvironmentData e WHERE e.isDeleted = false AND " +
           "e.collectedAt BETWEEN :startTime AND :endTime AND " +
           "((e.minThreshold IS NOT NULL AND e.value < e.minThreshold) OR " +
           "(e.maxThreshold IS NOT NULL AND e.value > e.maxThreshold)) " +
           "ORDER BY e.collectedAt DESC")
    Page<EnvironmentData> findOutOfRangeData(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    /**
     * 获取指定时间范围内的平均数据
     */
    @Query("SELECT e.dataType, AVG(e.value) FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.dataType = :dataType AND " +
           "e.collectedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY e.dataType")
    Optional<BigDecimal> findAverageByDataTypeAndTimeRange(@Param("dataType") EnvironmentData.DataType dataType,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定时间范围内的最大值
     */
    @Query("SELECT e.dataType, MAX(e.value) FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.dataType = :dataType AND " +
           "e.collectedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY e.dataType")
    Optional<BigDecimal> findMaxByDataTypeAndTimeRange(@Param("dataType") EnvironmentData.DataType dataType,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定时间范围内的最小值
     */
    @Query("SELECT e.dataType, MIN(e.value) FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.dataType = :dataType AND " +
           "e.collectedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY e.dataType")
    Optional<BigDecimal> findMinByDataTypeAndTimeRange(@Param("dataType") EnvironmentData.DataType dataType,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的数据点数量
     */
    Long countByDataTypeAndCollectedAtBetweenAndIsDeletedFalse(
            EnvironmentData.DataType dataType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据数据类型分组统计数据量
     */
    @Query("SELECT e.dataType, COUNT(e) FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.collectedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY e.dataType")
    List<Object[]> countByDataTypeGroupBy(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定传感器ID列表的最新数据
     */
    @Query("SELECT e FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.sensorId IN :sensorIds AND " +
           "e.id IN (SELECT MAX(e2.id) FROM EnvironmentData e2 WHERE " +
           "e2.isDeleted = false AND e2.sensorId = e.sensorId GROUP BY e2.sensorId)")
    List<EnvironmentData> findLatestBySensorIds(@Param("sensorIds") List<String> sensorIds);

    /**
     * 删除指定时间之前的数据（用于数据清理）
     */
    @Query("UPDATE EnvironmentData e SET e.deletedAt = CURRENT_TIMESTAMP, e.isDeleted = true WHERE " +
           "e.isDeleted = false AND e.collectedAt < :beforeTime")
    int softDeleteOldData(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询最近N小时的数据（用于图表显示）
     */
    @Query("SELECT e FROM EnvironmentData e WHERE " +
           "e.isDeleted = false AND e.dataType = :dataType AND " +
           "e.collectedAt >= :since ORDER BY e.collectedAt ASC")
    List<EnvironmentData> findRecentDataByHours(@Param("dataType") EnvironmentData.DataType dataType,
                                               @Param("since") LocalDateTime since);

    /**
     * 检查传感器是否存在
     */
    boolean existsBySensorIdAndIsDeletedFalse(String sensorId);

    /**
     * 查询所有活跃的传感器ID
     */
    @Query("SELECT DISTINCT e.sensorId FROM EnvironmentData e WHERE e.isDeleted = false")
    List<String> findAllActiveSensorIds();
}