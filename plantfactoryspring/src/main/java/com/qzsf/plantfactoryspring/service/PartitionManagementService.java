package com.qzsf.plantfactoryspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 数据库分区管理服务
 * 负责自动创建和清理分区
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartitionManagementService {

    private final JdbcTemplate jdbcTemplate;

    // 需要分区的表
    private static final List<String> PARTITIONED_TABLES = List.of(
        "environment_data",
        "operation_logs"
    );

    // 分区保留月数
    private static final int RETENTION_MONTHS = 12;

    // 提前创建分区的月数
    private static final int PRECREATE_MONTHS = 3;

    /**
     * 初始化分区管理
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("初始化数据库分区管理");

            // 暂时禁用分区管理以允许系统正常启动
            log.info("分区管理已暂时禁用，系统将正常启动但不使用分区功能");

            // TODO: 在数据库表正确配置分区后重新启用
            // enableEventScheduler();
            // checkAndCreatePartitions();

            log.info("数据库分区管理初始化完成（已禁用）");
        } catch (Exception e) {
            log.error("数据库分区管理初始化失败", e);
        }
    }

    /**
     * 启用事件调度器
     */
    public void enableEventScheduler() {
        try {
            jdbcTemplate.execute("SET GLOBAL event_scheduler = ON");
            log.info("事件调度器已启用");
        } catch (Exception e) {
            log.error("启用事件调度器失败", e);
            throw new RuntimeException("启用事件调度器失败", e);
        }
    }

    /**
     * 检查并创建分区
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public void checkAndCreatePartitions() {
        log.info("开始检查和创建数据库分区");

        for (String tableName : PARTITIONED_TABLES) {
            try {
                checkAndCreateTablePartitions(tableName);
            } catch (Exception e) {
                log.error("检查表 {} 分区失败", tableName, e);
            }
        }

        log.info("数据库分区检查和创建完成");
    }

    /**
     * 检查并清理旧分区
     */
    @Scheduled(cron = "0 0 3 1 * ?") // 每月1号凌晨3点执行
    @Transactional
    public void cleanupOldPartitions() {
        log.info("开始清理旧数据库分区");

        for (String tableName : PARTITIONED_TABLES) {
            try {
                cleanupTableOldPartitions(tableName);
            } catch (Exception e) {
                log.error("清理表 {} 旧分区失败", tableName, e);
            }
        }

        log.info("旧数据库分区清理完成");
    }

    /**
     * 检查并创建特定表的分区
     */
    private void checkAndCreateTablePartitions(String tableName) {
        log.debug("检查表 {} 的分区状态", tableName);

        // 获取当前表的分区信息
        List<Map<String, Object>> partitions = getTablePartitions(tableName);

        LocalDate currentDate = LocalDate.now();

        // 检查未来几个月的分区是否存在
        for (int i = 0; i <= PRECREATE_MONTHS; i++) {
            LocalDate targetDate = currentDate.plusMonths(i);
            String partitionName = generatePartitionName(targetDate);

            boolean partitionExists = partitions.stream()
                .anyMatch(p -> partitionName.equals(p.get("PARTITION_NAME")));

            if (!partitionExists) {
                log.info("创建表 {} 的新分区: {}", tableName, partitionName);
                createPartition(tableName, partitionName, targetDate);
            }
        }
    }

    /**
     * 清理特定表的旧分区
     */
    private void cleanupTableOldPartitions(String tableName) {
        log.debug("清理表 {} 的旧分区", tableName);

        List<Map<String, Object>> partitions = getTablePartitions(tableName);
        LocalDate cutoffDate = LocalDate.now().minusMonths(RETENTION_MONTHS);

        for (Map<String, Object> partition : partitions) {
            String partitionName = (String) partition.get("PARTITION_NAME");

            // 跳过未来分区
            if ("p_future".equals(partitionName)) {
                continue;
            }

            try {
                LocalDate partitionDate = parsePartitionDate(partitionName);

                if (partitionDate != null && partitionDate.isBefore(cutoffDate)) {
                    log.info("删除表 {} 的旧分区: {} (日期: {})", tableName, partitionName, partitionDate);
                    dropPartition(tableName, partitionName);

                    // 记录操作日志
                    logPartitionOperation(tableName, partitionName, "PARTITION_DROP",
                        "删除旧分区，日期: " + partitionDate);
                }
            } catch (Exception e) {
                log.warn("解析分区 {} 日期失败，跳过删除", partitionName, e);
            }
        }
    }

    /**
     * 创建分区
     */
    private void createPartition(String tableName, String partitionName, LocalDate partitionDate) {
        LocalDate nextMonth = partitionDate.plusMonths(1);
        String nextMonthStr = nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String sql = String.format(
            "ALTER TABLE %s REORGANIZE PARTITION p_future INTO (" +
            "PARTITION %s VALUES LESS THAN (TO_DAYS('%s'))," +
            "PARTITION p_future VALUES LESS THAN (MAXVALUE))",
            tableName, partitionName, nextMonthStr
        );

        try {
            jdbcTemplate.execute(sql);
            log.info("成功创建分区: {}.{}", tableName, partitionName);

            // 记录操作日志
            logPartitionOperation(tableName, partitionName, "PARTITION_CREATE",
                "创建新分区，日期: " + partitionDate);
        } catch (Exception e) {
            log.error("创建分区失败: {}.{}", tableName, partitionName, e);
            throw new RuntimeException("创建分区失败", e);
        }
    }

    /**
     * 删除分区
     */
    private void dropPartition(String tableName, String partitionName) {
        String sql = String.format("ALTER TABLE %s DROP PARTITION %s", tableName, partitionName);

        try {
            jdbcTemplate.execute(sql);
            log.info("成功删除分区: {}.{}", tableName, partitionName);
        } catch (Exception e) {
            log.error("删除分区失败: {}.{}", tableName, partitionName, e);
            throw new RuntimeException("删除分区失败", e);
        }
    }

    /**
     * 获取表的分区信息
     */
    private List<Map<String, Object>> getTablePartitions(String tableName) {
        String sql = """
            SELECT PARTITION_NAME, PARTITION_DESCRIPTION, TABLE_ROWS,
                   DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME
            FROM information_schema.PARTITIONS
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = ?
            AND PARTITION_NAME IS NOT NULL
            ORDER BY PARTITION_NAME
            """;

        return jdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * 生成分区名称
     */
    private String generatePartitionName(LocalDate date) {
        return "p_" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * 解析分区名称中的日期
     */
    private LocalDate parsePartitionDate(String partitionName) {
        if (partitionName == null || !partitionName.startsWith("p_")) {
            return null;
        }

        try {
            String dateStr = partitionName.substring(2); // 去掉 "p_" 前缀
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMM"));
        } catch (Exception e) {
            log.warn("解析分区名称 {} 中的日期失败", partitionName, e);
            return null;
        }
    }

    /**
     * 记录分区操作日志
     */
    private void logPartitionOperation(String tableName, String partitionName,
                                     String operationType, String description) {
        try {
            String sql = """
                INSERT INTO operation_logs (
                    module_name, operation_type, operation_desc, status, created_at
                ) VALUES (?, ?, ?, 'SUCCESS', NOW())
                """;

            jdbcTemplate.update(sql, "DATABASE", operationType,
                String.format("%s: %s.%s - %s", operationType, tableName, partitionName, description));
        } catch (Exception e) {
            // 日志记录失败不应该影响主要操作
            log.warn("记录分区操作日志失败", e);
        }
    }

    /**
     * 获取分区监控信息
     */
    public List<Map<String, Object>> getPartitionMonitoringInfo() {
        String sql = """
            SELECT
                TABLE_NAME,
                PARTITION_NAME,
                PARTITION_DESCRIPTION,
                TABLE_ROWS,
                ROUND(DATA_LENGTH / 1024 / 1024, 2) as data_size_mb,
                ROUND(INDEX_LENGTH / 1024 / 1024, 2) as index_size_mb,
                ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as total_size_mb,
                CREATE_TIME,
                UPDATE_TIME,
                CASE
                    WHEN PARTITION_NAME = 'p_future' THEN 'Future'
                    ELSE 'Active'
                END as status
            FROM information_schema.PARTITIONS
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME IN ('environment_data', 'operation_logs')
            AND PARTITION_NAME IS NOT NULL
            ORDER BY TABLE_NAME, PARTITION_NAME
            """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 手动创建指定月份的分区
     */
    @Transactional
    public void createPartitionForMonth(String tableName, int year, int month) {
        if (!PARTITIONED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("不支持的表: " + tableName);
        }

        LocalDate partitionDate = LocalDate.of(year, month, 1);
        String partitionName = generatePartitionName(partitionDate);

        // 检查分区是否已存在
        List<Map<String, Object>> partitions = getTablePartitions(tableName);
        boolean exists = partitions.stream()
            .anyMatch(p -> partitionName.equals(p.get("PARTITION_NAME")));

        if (exists) {
            log.info("分区已存在: {}.{}", tableName, partitionName);
            return;
        }

        createPartition(tableName, partitionName, partitionDate);
    }

    /**
     * 手动清理旧分区
     */
    @Transactional
    public void cleanupPartitionsOlderThan(String tableName, int months) {
        if (!PARTITIONED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("不支持的表: " + tableName);
        }

        List<Map<String, Object>> partitions = getTablePartitions(tableName);
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);

        for (Map<String, Object> partition : partitions) {
            String partitionName = (String) partition.get("PARTITION_NAME");

            if ("p_future".equals(partitionName)) {
                continue;
            }

            try {
                LocalDate partitionDate = parsePartitionDate(partitionName);

                if (partitionDate != null && partitionDate.isBefore(cutoffDate)) {
                    dropPartition(tableName, partitionName);
                    logPartitionOperation(tableName, partitionName, "MANUAL_PARTITION_DROP",
                        "手动删除旧分区，保留月数: " + months);
                }
            } catch (Exception e) {
                log.warn("解析分区 {} 日期失败，跳过删除", partitionName, e);
            }
        }
    }
}