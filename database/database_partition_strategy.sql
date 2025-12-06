-- 植物工厂管理系统 - 数据库分区策略优化
--
-- 问题修复:
-- 1. 原分区策略无法自动创建新分区
-- 2. 缺少分区删除机制
-- 3. 性能优化不足
--
-- 解决方案:
-- 1. 使用事件调度器自动创建和删除分区
-- 2. 按月分区，保留12个月数据
-- 3. 添加分区管理存储过程

-- ======================================
-- 1. 环境数据表优化分区策略
-- ======================================

-- 删除现有的错误分区表（如果存在）
DROP TABLE IF EXISTS environment_data;

-- 重新创建环境数据表，采用按月分区策略
CREATE TABLE environment_data (
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

    INDEX idx_area_time (area_id, data_time),
    INDEX idx_data_time (data_time),
    INDEX idx_quality_time (quality_flag, data_time),
    INDEX idx_area_created (area_id, created_at)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='环境数据表 - 按月自动分区'
PARTITION BY RANGE (TO_DAYS(data_time)) (
    -- 创建当前月和下个月的分区
    PARTITION p_202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p_202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p_202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p_202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p_202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p_202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
);

-- ======================================
-- 2. 操作日志表分区策略
-- ======================================

-- 删除现有的操作日志表（如果存在）
DROP TABLE IF EXISTS operation_logs;

-- 重新创建操作日志表，采用按月分区策略
CREATE TABLE operation_logs (
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
    INDEX idx_status_created (status, created_at)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='操作日志表 - 按月自动分区'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    -- 创建当前月和下个月的分区
    PARTITION p_202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p_202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p_202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p_202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p_202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p_202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
);

-- ======================================
-- 3. 分区管理存储过程
-- ======================================

DELIMITER //

-- 创建新分区的存储过程
CREATE PROCEDURE create_partition(
    IN table_name VARCHAR(64),
    IN partition_name VARCHAR(64),
    IN partition_date DATE
)
BEGIN
    DECLARE next_month DATE;
    DECLARE partition_limit DATE;
    DECLARE sql_text TEXT;

    -- 计算下个月的第一天
    SET next_month = DATE_ADD(partition_date, INTERVAL 1 MONTH);
    SET next_month = DATE_FORMAT(next_month, '%Y-%m-01');

    -- 计算分区上限
    SET partition_limit = next_month;

    -- 构建添加分区的SQL
    SET sql_text = CONCAT(
        'ALTER TABLE ', table_name,
        ' REORGANIZE PARTITION p_future INTO (',
        'PARTITION ', partition_name, ' VALUES LESS THAN (TO_DAYS(\'', partition_limit, '\')),',
        'PARTITION p_future VALUES LESS THAN (MAXVALUE)'
        ')'
    );

    -- 执行SQL
    SET @sql = sql_text;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 记录操作日志
    INSERT INTO operation_logs (
        module_name,
        operation_type,
        operation_desc,
        status,
        created_at
    ) VALUES (
        'DATABASE',
        'PARTITION_CREATE',
        CONCAT('创建分区 ', table_name, '.', partition_name),
        'SUCCESS',
        NOW()
    );

    SELECT CONCAT('成功创建分区: ', table_name, '.', partition_name) AS result;
END //

-- 删除旧分区的存储过程
CREATE PROCEDURE drop_old_partitions(
    IN table_name VARCHAR(64),
    IN retention_months INT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(64);
    DECLARE partition_description TEXT;
    DECLARE partition_date DATE;
    DECLARE cutoff_date DATE;

    -- 分区信息游标
    DECLARE partition_cursor CURSOR FOR
        SELECT
            PARTITION_NAME,
            PARTITION_DESCRIPTION
        FROM information_schema.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = table_name
        AND PARTITION_NAME IS NOT NULL
        AND PARTITION_NAME != 'p_future'
        ORDER BY PARTITION_NAME;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- 计算保留截止日期
    SET cutoff_date = DATE_SUB(NOW(), INTERVAL retention_months MONTH);

    OPEN partition_cursor;

    read_loop: LOOP
        FETCH partition_cursor INTO partition_name, partition_description;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 尝试从分区描述中提取日期
        -- 这里假设分区描述包含日期信息
        BEGIN
            DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;

            -- 简单的日期提取逻辑（根据实际情况调整）
            IF partition_name REGEXP '^p_[0-9]{6}$' THEN
                SET partition_date = STR_TO_DATE(SUBSTRING(partition_name, 3), '%Y%m');

                -- 如果分区日期早于保留截止日期，则删除
                IF partition_date < cutoff_date THEN
                    SET @sql = CONCAT('ALTER TABLE ', table_name, ' DROP PARTITION ', partition_name);
                    PREPARE stmt FROM @sql;
                    EXECUTE stmt;
                    DEALLOCATE PREPARE stmt;

                    -- 记录操作日志
                    INSERT INTO operation_logs (
                        module_name,
                        operation_type,
                        operation_desc,
                        status,
                        created_at
                    ) VALUES (
                        'DATABASE',
                        'PARTITION_DROP',
                        CONCAT('删除分区 ', table_name, '.', partition_name),
                        'SUCCESS',
                        NOW()
                    );
                END IF;
            END IF;
        END;
    END LOOP;

    CLOSE partition_cursor;

    SELECT CONCAT('清理完成，保留最近 ', retention_months, ' 个月的数据') AS result;
END //

-- 获取分区信息的存储过程
CREATE PROCEDURE get_partition_info(
    IN table_name VARCHAR(64)
)
BEGIN
    SELECT
        PARTITION_NAME,
        PARTITION_DESCRIPTION,
        TABLE_ROWS,
        AVG_ROW_LENGTH,
        DATA_LENGTH,
        INDEX_LENGTH,
        CREATE_TIME,
        UPDATE_TIME
    FROM information_schema.PARTITIONS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = table_name
    ORDER BY PARTITION_NAME;
END //

DELIMITER ;

-- ======================================
-- 4. 自动化管理事件调度器
-- ======================================

-- 开启事件调度器
SET GLOBAL event_scheduler = ON;

-- 每月自动创建分区的事件
CREATE EVENT IF NOT EXISTS auto_create_monthly_partitions
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(CONCAT(YEAR(NOW()), '-', MONTH(NOW()), '-', '28', ' 23:00:00'))
DO
BEGIN
    DECLARE next_month VARCHAR(7);
    DECLARE next_month_date DATE;

    -- 计算下个月
    SET next_month = DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 MONTH), '%Y%m');
    SET next_month_date = DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 MONTH), '%Y-%m-01');

    -- 为环境数据表创建分区
    CALL create_partition('environment_data', CONCAT('p_', next_month), next_month_date);

    -- 为操作日志表创建分区
    CALL create_partition('operation_logs', CONCAT('p_', next_month), next_month_date);

    -- 记录自动创建分区的日志
    INSERT INTO operation_logs (
        module_name,
        operation_type,
        operation_desc,
        status,
        created_at
    ) VALUES (
        'DATABASE',
        'AUTO_PARTITION_CREATE',
        CONCAT('自动创建月度分区: ', next_month),
        'SUCCESS',
        NOW()
    );
END;

-- 每季度自动清理旧分区的事件
CREATE EVENT IF NOT EXISTS auto_cleanup_old_partitions
ON SCHEDULE EVERY 3 MONTH
STARTS TIMESTAMP(CONCAT(YEAR(NOW()), '-', MONTH(NOW()), '-', '01', ' 02:00:00'))
DO
BEGIN
    -- 清理12个月前的分区，保留12个月数据
    CALL drop_old_partitions('environment_data', 12);
    CALL drop_old_partitions('operation_logs', 12);

    -- 记录自动清理分区的日志
    INSERT INTO operation_logs (
        module_name,
        operation_type,
        operation_desc,
        status,
        created_at
    ) VALUES (
        'DATABASE',
        'AUTO_PARTITION_CLEANUP',
        '自动清理12个月前的分区',
        'SUCCESS',
        NOW()
    );
END;

-- ======================================
-- 5. 分区监控视图
-- ======================================

-- 创建分区监控视图
CREATE VIEW partition_monitor AS
SELECT
    TABLE_NAME,
    PARTITION_NAME,
    PARTITION_DESCRIPTION,
    TABLE_ROWS,
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS data_size_mb,
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS index_size_mb,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS total_size_mb,
    CREATE_TIME,
    UPDATE_TIME,
    CASE
        WHEN PARTITION_NAME = 'p_future' THEN 'Future'
        ELSE 'Active'
    END AS status
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('environment_data', 'operation_logs')
AND PARTITION_NAME IS NOT NULL
ORDER BY TABLE_NAME, PARTITION_NAME;

-- ======================================
-- 6. 初始化脚本
-- ======================================

-- 手动创建当前月份之后的分区（初始化）
DO
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE current_date DATE;
    DECLARE partition_name VARCHAR(64);
    DECLARE partition_limit DATE;

    SET current_date = DATE_FORMAT(NOW(), '%Y-%m-01');

    -- 创建未来6个月的分区
    WHILE i <= 6 DO
        SET partition_name = CONCAT('p_', DATE_FORMAT(DATE_ADD(current_date, INTERVAL i MONTH), '%Y%m'));
        SET partition_limit = DATE_ADD(DATE_ADD(current_date, INTERVAL i MONTH), INTERVAL 1 MONTH);

        -- 为环境数据表创建分区
        SET @sql = CONCAT(
            'ALTER TABLE environment_data REORGANIZE PARTITION p_future INTO (',
            'PARTITION ', partition_name, ' VALUES LESS THAN (TO_DAYS(\'', partition_limit, '\')),',
            'PARTITION p_future VALUES LESS THAN (MAXVALUE)'
            ')'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        -- 为操作日志表创建分区
        SET @sql = CONCAT(
            'ALTER TABLE operation_logs REORGANIZE PARTITION p_future INTO (',
            'PARTITION ', partition_name, ' VALUES LESS THAN (TO_DAYS(\'', partition_limit, '\')),',
            'PARTITION p_future VALUES LESS THAN (MAXVALUE)'
            ')'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SET i = i + 1;
    END WHILE;
END;

-- ======================================
-- 7. 使用示例和测试
-- ======================================

-- 测试分区信息查询
-- CALL get_partition_info('environment_data');

-- 手动创建新分区
-- CALL create_partition('environment_data', 'p_202507', '2025-07-01');

-- 手动清理旧分区
-- CALL drop_old_partitions('environment_data', 6);

-- 查看分区监控视图
-- SELECT * FROM partition_monitor WHERE TABLE_NAME = 'environment_data';

-- ======================================
-- 8. 性能优化建议
-- ======================================

/*
分区策略优化要点:

1. 分区键选择:
   - 使用data_time和created_at作为分区键
   - 按月分区，平衡查询性能和管理复杂度

2. 自动化运维:
   - 每月自动创建新分区
   - 每季度自动清理旧分区
   - 保留12个月数据

3. 监控和维护:
   - 分区监控视图
   - 操作日志记录
   - 存储过程管理

4. 性能优化:
   - 分区裁剪: 查询时利用分区键
   - 本地索引: 每个分区独立索引
   - 批量操作: 减少分区切换开销

5. 数据保留策略:
   - 环境数据: 保留12个月
   - 操作日志: 保留12个月
   - 可根据需要调整保留期

6. 灾难恢复:
   - 分区可独立备份和恢复
   - 支持分区级别的数据导入导出
*/

-- 最终确认事件调度器已开启
SET GLOBAL event_scheduler = ON;

-- 显示所有事件
SHOW EVENTS;

-- 显示分区信息
SELECT * FROM partition_monitor;