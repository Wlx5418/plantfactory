-- 植物工厂管理系统数据库初始化脚本
-- 基于设计文档创建完整的数据表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS plant_factory
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE plant_factory;

-- =====================================
-- 1. 用户管理相关表
-- =====================================

-- 1.1 用户表 (users)
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
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表 (roles)
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

-- 1.3 权限表 (permissions)
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

-- 1.4 用户角色关联表 (user_roles)
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

-- 1.5 角色权限关联表 (role_permissions)
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================
-- 2. 基础数据管理表
-- =====================================

-- 2.1 生产区域表 (production_areas)
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
    INDEX idx_status (status),
    INDEX idx_capacity (capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产区域表';

-- 2.2 设备配置表 (device_configs)
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
    parameters JSON COMMENT '设备参数配置（温度范围、湿度阈值等）',
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
    INDEX idx_status (status),
    INDEX idx_data_source (data_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备配置表';

-- 2.3 环境数据表 (environment_data)
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
    INDEX idx_quality_time (quality_flag, data_time),
    INDEX idx_data_source (data_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境数据表';

-- =====================================
-- 3. 初始化数据
-- =====================================

-- 3.1 初始化权限数据
INSERT INTO permissions (name, code, description, resource_type, resource_path, http_method, parent_id, sort_order) VALUES
('系统管理', 'system:manage', '系统管理模块权限', 'MENU', '/system', null, null, 1),
('用户管理', 'user:manage', '用户管理模块权限', 'MENU', '/system/users', null, 1, 2),
('用户查看', 'user:view', '查看用户列表权限', 'BUTTON', null, null, 2, 3),
('用户创建', 'user:create', '创建用户权限', 'BUTTON', null, null, 2, 4),
('用户编辑', 'user:update', '编辑用户权限', 'BUTTON', null, null, 2, 5),
('用户删除', 'user:delete', '删除用户权限', 'BUTTON', null, null, 2, 6),

-- 环境监控权限
('环境监控', 'environment:monitor', '环境监控模块权限', 'MENU', '/environment', null, null, 7),
('环境数据', 'environment:data', '环境数据管理权限', 'MENU', '/environment/data', null, 7, 8),
('环境数据查看', 'environment:view', '查看环境数据权限', 'BUTTON', null, null, 8, 9),
('环境数据录入', 'environment:create', '录入环境数据权限', 'BUTTON', null, null, 8, 10),

-- 设备管理权限
('设备管理', 'device:manage', '设备管理模块权限', 'MENU', '/devices', null, null, 11),
('设备查看', 'device:view', '查看设备列表权限', 'BUTTON', null, null, 11, 12),
('设备控制', 'device:control', '设备控制权限', 'BUTTON', null, null, 11, 13),

-- API权限
('认证登录', 'auth:login', '用户登录API权限', 'API', '/auth/login', 'POST', null, 14),
('认证登出', 'auth:logout', '用户登出API权限', 'API', '/auth/logout', 'POST', null, 15),
('令牌刷新', 'auth:refresh', '刷新令牌API权限', 'API', '/auth/refresh', 'POST', null, 16),
('用户信息', 'auth:me', '获取用户信息API权限', 'API', '/auth/me', 'GET', null, 17);

-- 3.2 初始化角色数据
INSERT INTO roles (role_name, role_code, description, status) VALUES
('系统管理员', 'ADMIN', '拥有系统所有权限', 'ACTIVE'),
('管理员', 'MANAGER', '拥有管理权限', 'ACTIVE'),
('操作员', 'OPERATOR', '拥有操作权限', 'ACTIVE'),
('观察员', 'OBSERVER', '只读权限', 'ACTIVE');

-- 3.3 为系统管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 3.4 为管理员角色分配大部分权限（不包括删除操作）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE code NOT IN ('user:delete', 'role:delete', 'environment:delete', 'device:delete', 'production:delete');

-- 3.5 为操作员分配操作权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view', 'environment:create',
    'device:manage', 'device:view', 'device:control',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 3.6 为观察员分配只读权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view',
    'device:manage', 'device:view',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 3.7 创建默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, real_name, status) VALUES
('admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'admin@plant-factory.com', '系统管理员', 'ACTIVE');

-- 3.8 为默认管理员分配系统管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 3.9 创建测试用户
INSERT INTO users (username, password, email, real_name, status) VALUES
('operator', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'operator@plant-factory.com', '操作员', 'ACTIVE'),
('observer', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'observer@plant-factory.com', '观察员', 'ACTIVE');

-- 3.10 为测试用户分配角色
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 3), -- operator -> OPERATOR
(3, 4); -- observer -> OBSERVER

-- 3.11 初始化生产区域
INSERT INTO production_areas (area_code, area_name, area_type, capacity, location) VALUES
('AREA001', '育苗区', 'SEEDING', 50.00, '东侧1层'),
('AREA002', '生长A区', 'GROWING', 200.00, '东侧2层'),
('AREA003', '生长B区', 'GROWING', 150.00, '东侧3层'),
('AREA004', '采收区', 'HARVESTING', 100.00, '西侧1层');

COMMIT;