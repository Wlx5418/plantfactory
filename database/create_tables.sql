-- 植物工厂管理系统 - 创建表结构脚本

-- 使用数据库
USE plant_factory;

-- 用户表 (users)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表 (roles)
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表 (permissions)
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    description VARCHAR(500) COMMENT '权限描述',
    resource_type ENUM('MENU', 'BUTTON', 'API') DEFAULT 'API' COMMENT '资源类型',
    resource_path VARCHAR(500) COMMENT '资源路径',
    http_method VARCHAR(20) COMMENT 'HTTP请求方法',
    parent_id BIGINT COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (code),
    INDEX idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表 (user_roles)
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表 (role_permissions)
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 生产区域表 (production_areas)
CREATE TABLE production_areas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
    area_code VARCHAR(50) NOT NULL UNIQUE COMMENT '区域编码',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    area_type ENUM('SEEDING', 'GROWING', 'FLOWERING', 'HARVESTING') DEFAULT 'GROWING' COMMENT '区域类型',
    capacity DECIMAL(8,2) NOT NULL COMMENT '容量(平方米)',
    current_usage DECIMAL(8,2) DEFAULT 0 COMMENT '当前使用面积(平方米)',
    max_capacity_plants INT COMMENT '最大植物数量',
    current_plant_count INT DEFAULT 0 COMMENT '当前植物数量',
    location VARCHAR(200) COMMENT '位置描述',
    description TEXT COMMENT '区域描述',
    environmental_config JSON COMMENT '环境配置参数',
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_area_code (area_code),
    INDEX idx_area_type (area_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产区域表';

-- 设备配置表 (device_configs)
CREATE TABLE device_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type ENUM('MONITOR', 'CONTROL', 'SENSOR') NOT NULL COMMENT '设备类型',
    device_category VARCHAR(50) COMMENT '设备分类',
    area_id BIGINT COMMENT '所属区域ID',
    location VARCHAR(200) COMMENT '设备位置',
    manufacturer VARCHAR(100) COMMENT '制造商',
    model VARCHAR(100) COMMENT '型号',
    parameters JSON COMMENT '设备参数配置',
    data_source ENUM('MANUAL', 'EXCEL_IMPORT', 'API') DEFAULT 'MANUAL' COMMENT '数据来源',
    maintenance_interval INT COMMENT '维护间隔(天)',
    last_maintenance_date DATE COMMENT '上次维护日期',
    status ENUM('NORMAL', 'MAINTENANCE', 'OFFLINE') DEFAULT 'NORMAL' COMMENT '设备状态',
    operator_id BIGINT COMMENT '最后操作人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (area_id) REFERENCES production_areas(id) ON DELETE SET NULL,
    FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_area_device (area_id, device_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备配置表';

-- 环境数据表 (environment_data)
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
    deleted_at DATETIME COMMENT '删除时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (area_id) REFERENCES production_areas(id),
    FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_area_time (area_id, data_time),
    INDEX idx_data_time (data_time),
    INDEX idx_quality_time (quality_flag, data_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境数据表';