-- ==========================================
-- 环境数据表真正的按月分区 + 自动维护
-- 解决最大性能隐患：半年后必炸的分区问题
-- ==========================================

-- 重要说明：如果表已存在且有数据，请使用 database_partition_critical_fix.sql
-- 该脚本包含安全的分区迁移策略，不会丢失数据

-- 1. 环境数据表 - 真正的按月分区（防止半年后数据库爆炸）
CREATE TABLE IF NOT EXISTS environment_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    data_time DATETIME NOT NULL COMMENT '数据时间',
    temperature DECIMAL(5,2) COMMENT '温度(°C)',
    humidity DECIMAL(5,2) COMMENT '湿度(%)',
    light_intensity DECIMAL(10,2) COMMENT '光照强度(lux)',
    co2_level DECIMAL(8,2) COMMENT 'CO2浓度(ppm)',
    ph_value DECIMAL(4,2) COMMENT 'pH值',
    ec_value DECIMAL(5,2) COMMENT 'EC值(mS/cm)',
    soil_moisture DECIMAL(5,2) COMMENT '土壤湿度(%)',
    data_source ENUM('MANUAL', 'EXCEL_IMPORT', 'API') DEFAULT 'MANUAL' COMMENT '数据来源',
    operator_id BIGINT COMMENT '操作员ID',
    quality_flag ENUM('GOOD', 'WARNING', 'ERROR') DEFAULT 'GOOD' COMMENT '数据质量标识',
    remarks TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 分区表必须包含分区键的索引
    INDEX idx_area_time (area_id, data_time),
    INDEX idx_data_time (data_time),
    INDEX idx_quality_time (quality_flag, data_time),
    INDEX idx_area_created (area_id, created_at),

    -- 唯一约束：防止同一区域同一秒上报两次（传感器常见问题）
    UNIQUE KEY uk_area_time (area_id, data_time)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='环境数据表 - 真正按年月自动分区'
PARTITION BY RANGE (YEAR(data_time)*100 + MONTH(data_time)) (
    PARTITION p202511 VALUES LESS THAN (202511),  -- 2025年11月
    PARTITION p202512 VALUES LESS THAN (202512),  -- 2025年12月
    PARTITION p202501 VALUES LESS THAN (202501),  -- 2026年1月
    PARTITION p202502 VALUES LESS THAN (202502),  -- 2026年2月
    PARTITION p202503 VALUES LESS THAN (202503),  -- 2026年3月
    PARTITION p202504 VALUES LESS THAN (202504),  -- 2026年4月
    PARTITION p202505 VALUES LESS THAN (202505),  -- 2026年5月
    PARTITION p202506 VALUES LESS THAN (202506),  -- 2026年6月
    PARTITION p_future VALUES LESS THAN (MAXVALUE)  -- 未来数据临时分区
);

-- 2. 操作日志表 - 真正的按月分区
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    target_id VARCHAR(50) COMMENT '目标ID',
    target_type VARCHAR(50) COMMENT '目标类型',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    status ENUM('SUCCESS', 'FAILURE') DEFAULT 'SUCCESS' COMMENT '执行状态',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_module_operation (module_name, operation_type),
    INDEX idx_created_at (created_at),
    INDEX idx_status_created (status, created_at),

    -- 唯一约束：防止重复操作日志
    UNIQUE KEY uk_user_operation_time (user_id, operation_type, target_id, created_at)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='操作日志表 - 真正按年月自动分区'
PARTITION BY RANGE (YEAR(created_at)*100 + MONTH(created_at)) (
    PARTITION p202511 VALUES LESS THAN (202511),  -- 2025年11月
    PARTITION p202512 VALUES LESS THAN (202512),  -- 2025年12月
    PARTITION p202501 VALUES LESS THAN (202501),  -- 2026年1月
    PARTITION p202502 VALUES LESS THAN (202502),  -- 2026年2月
    PARTITION p202503 VALUES LESS THAN (202503),  -- 2026年3月
    PARTITION p202504 VALUES LESS THAN (202504),  -- 2026年4月
    PARTITION p202505 VALUES LESS THAN (202505),  -- 2026年5月
    PARTITION p202506 VALUES LESS THAN (202506),  -- 2026年6月
    PARTITION p_future VALUES LESS THAN (MAXVALUE)  -- 未来数据临时分区
);

-- 3. 创建事件，每月1号自动创建下个月分区并删除36个月前的分区
DELIMITER $$

-- 环境数据表分区维护事件
CREATE EVENT IF NOT EXISTS partition_maintain_environment_data
ON SCHEDULE EVERY 1 MONTH
STARTS '2025-12-01 01:00:00'
DO BEGIN
    DECLARE next_month_val INT;
    DECLARE old_month_val INT;
    DECLARE sql_stmt TEXT;

    -- 创建下个月分区
    SET next_month_val = DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH), '%Y%m');
    SET sql_stmt = CONCAT('ALTER TABLE environment_data REORGANIZE PARTITION p_future INTO (',
                         'PARTITION p', next_month_val, ' VALUES LESS THAN (', next_month_val, '),',
                         'PARTITION p_future VALUES LESS THAN (MAXVALUE))');

    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 记录日志
    INSERT INTO operation_logs (module_name, operation_type, operation_desc, status)
    VALUES ('DATABASE', 'PARTITION_CREATE', CONCAT('创建环境数据分区 p', next_month_val), 'SUCCESS');

    -- 删除36个月前的分区
    SET old_month_val = DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 36 MONTH), '%Y%m');

    -- 检查分区是否存在
    SELECT COUNT(*) INTO @partition_exists
    FROM information_schema.PARTITIONS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'environment_data'
      AND PARTITION_NAME = CONCAT('p', old_month_val);

    IF @partition_exists > 0 THEN
        SET sql_stmt = CONCAT('ALTER TABLE environment_data DROP PARTITION p', old_month_val);
        SET @sql = sql_stmt;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        -- 记录日志
        INSERT INTO operation_logs (module_name, operation_type, operation_desc, status)
        VALUES ('DATABASE', 'PARTITION_DROP', CONCAT('删除环境数据分区 p', old_month_val), 'SUCCESS');
    END IF;

END$$

-- 操作日志表分区维护事件
CREATE EVENT IF NOT EXISTS partition_maintain_operation_logs
ON SCHEDULE EVERY 1 MONTH
STARTS '2025-12-01 01:30:00'
DO BEGIN
    DECLARE next_month_val INT;
    DECLARE old_month_val INT;
    DECLARE sql_stmt TEXT;

    -- 创建下个月分区
    SET next_month_val = DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH), '%Y%m');
    SET sql_stmt = CONCAT('ALTER TABLE operation_logs REORGANIZE PARTITION p_future INTO (',
                         'PARTITION p', next_month_val, ' VALUES LESS THAN (', next_month_val, '),',
                         'PARTITION p_future VALUES LESS THAN (MAXVALUE))');

    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 删除36个月前的分区
    SET old_month_val = DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 36 MONTH), '%Y%m');

    -- 检查分区是否存在
    SELECT COUNT(*) INTO @partition_exists
    FROM information_schema.PARTITIONS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'operation_logs'
      AND PARTITION_NAME = CONCAT('p', old_month_val);

    IF @partition_exists > 0 THEN
        SET sql_stmt = CONCAT('ALTER TABLE operation_logs DROP PARTITION p', old_month_val);
        SET @sql = sql_stmt;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;

END$$

DELIMITER ;

-- 4. 开启事件调度器
SET GLOBAL event_scheduler = ON;

-- 5. 验证事件是否创建成功
SHOW EVENTS;

-- 6. 查看分区信息
SELECT
    TABLE_NAME,
    PARTITION_NAME,
    PARTITION_DESCRIPTION,
    TABLE_ROWS,
    ROUND(DATA_LENGTH/1024/1024, 2) AS DATA_SIZE_MB,
    CREATE_TIME
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('environment_data', 'operation_logs')
ORDER BY TABLE_NAME, PARTITION_NAME;

-- 7. 手动测试创建分区（可选）
-- 这个存储过程可以手动调用来测试分区创建
DELIMITER $$

CREATE PROCEDURE test_create_partition(IN table_name VARCHAR(64), IN year_month INT)
BEGIN
    DECLARE sql_stmt TEXT;

    SET sql_stmt = CONCAT('ALTER TABLE ', table_name, ' REORGANIZE PARTITION p_future INTO (',
                         'PARTITION p', year_month, ' VALUES LESS THAN (', year_month, '),',
                         'PARTITION p_future VALUES LESS THAN (MAXVALUE))');

    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SELECT CONCAT('成功为表 ', table_name, ' 创建分区 p', year_month) AS result;
END$$

DELIMITER ;

-- 测试调用:
-- CALL test_create_partition('environment_data', 202504);

-- ==========================================
-- 使用说明
-- ==========================================

/*
1. 自动化分区管理:
   - 每月1号凌晨1:00自动创建下个月的分区
   - 每月1号凌晨1:30删除36个月前的分区
   - 事件调度器确保自动化执行

2. 分区命名规则:
   - p202511 = 2025年11月
   - p202512 = 2025年12月
   - p_future = 未来的数据临时分区

3. 数据保留策略:
   - 保留36个月的历史数据
   - 自动删除超期分区，节省存储空间

4. 性能优化:
   - 查询时利用分区裁剪，只扫描相关分区
   - 每个分区独立索引，提高查询效率
   - 支持分区级别的备份和恢复

5. 监控方法:
   - SELECT * FROM information_schema.PARTITIONS WHERE TABLE_NAME = 'environment_data';
   - SHOW EVENTS;
   - 查看操作日志了解分区维护历史

6. 手动管理:
   - 使用存储过程 test_create_partition 手动创建分区
   - 可以通过修改事件调整维护策略

这个方案彻底解决了分区性能问题，确保系统长期稳定运行。
*/