-- 植物工厂管理系统认证模块数据库初始化脚本
-- 创建时间: 2025-11-12
-- 版本: v1.0

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS plant_factory
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE plant_factory;

-- 1. 权限表 (permissions)
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
    INDEX idx_resource_type (resource_type),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 2. 角色表 (roles)
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

-- 3. 角色权限关联表 (role_permissions)
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 4. 用户表 (users)
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
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 5. 用户角色关联表 (user_roles)
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

-- 插入基础权限数据
INSERT INTO permissions (name, code, description, resource_type, resource_path, http_method, parent_id, sort_order) VALUES
-- 系统管理权限
('系统管理', 'system:manage', '系统管理模块权限', 'MENU', '/system', null, null, 1),
('用户管理', 'user:manage', '用户管理模块权限', 'MENU', '/system/users', null, 1, 2),
('用户查看', 'user:view', '查看用户列表权限', 'BUTTON', null, null, 2, 3),
('用户创建', 'user:create', '创建用户权限', 'BUTTON', null, null, 2, 4),
('用户编辑', 'user:update', '编辑用户权限', 'BUTTON', null, null, 2, 5),
('用户删除', 'user:delete', '删除用户权限', 'BUTTON', null, null, 2, 6),
('角色管理', 'role:manage', '角色管理模块权限', 'MENU', '/system/roles', null, 1, 7),
('角色查看', 'role:view', '查看角色列表权限', 'BUTTON', null, null, 7, 8),
('角色创建', 'role:create', '创建角色权限', 'BUTTON', null, null, 7, 9),
('角色编辑', 'role:update', '编辑角色权限', 'BUTTON', null, null, 7, 10),
('角色删除', 'role:delete', '删除角色权限', 'BUTTON', null, null, 7, 11),

-- 环境监控权限
('环境监控', 'environment:monitor', '环境监控模块权限', 'MENU', '/environment', null, null, 12),
('环境数据', 'environment:data', '环境数据管理权限', 'MENU', '/environment/data', null, 12, 13),
('环境数据查看', 'environment:view', '查看环境数据权限', 'BUTTON', null, null, 13, 14),
('环境数据录入', 'environment:create', '录入环境数据权限', 'BUTTON', null, null, 13, 15),
('环境数据编辑', 'environment:update', '编辑环境数据权限', 'BUTTON', null, null, 13, 16),
('环境数据删除', 'environment:delete', '删除环境数据权限', 'BUTTON', null, null, 13, 17),

-- 设备管理权限
('设备管理', 'device:manage', '设备管理模块权限', 'MENU', '/devices', null, null, 18),
('设备查看', 'device:view', '查看设备列表权限', 'BUTTON', null, null, 18, 19),
('设备创建', 'device:create', '创建设备权限', 'BUTTON', null, null, 18, 20),
('设备编辑', 'device:update', '编辑设备权限', 'BUTTON', null, null, 18, 21),
('设备删除', 'device:delete', '删除设备权限', 'BUTTON', null, null, 18, 22),
('设备控制', 'device:control', '设备控制权限', 'BUTTON', null, null, 18, 23),

-- 生产管理权限
('生产管理', 'production:manage', '生产管理模块权限', 'MENU', '/production', null, null, 24),
('生产计划', 'production:plan', '生产计划管理权限', 'MENU', '/production/plans', null, 24, 25),
('生产计划查看', 'production:view', '查看生产计划权限', 'BUTTON', null, null, 25, 26),
('生产计划创建', 'production:create', '创建生产计划权限', 'BUTTON', null, null, 25, 27),
('生产计划编辑', 'production:update', '编辑生产计划权限', 'BUTTON', null, null, 25, 28),
('生产计划删除', 'production:delete', '删除生产计划权限', 'BUTTON', null, null, 25, 29),

-- 数据报表权限
('数据报表', 'report:view', '数据报表查看权限', 'MENU', '/reports', null, null, 30),
('数据统计', 'statistics:view', '数据统计查看权限', 'BUTTON', null, null, 30, 31),

-- API权限（自动生成）
('认证登录', 'auth:login', '用户登录API权限', 'API', '/auth/login', 'POST', null, 32),
('认证登出', 'auth:logout', '用户登出API权限', 'API', '/auth/logout', 'POST', null, 33),
('令牌刷新', 'auth:refresh', '刷新令牌API权限', 'API', '/auth/refresh', 'POST', null, 34),
('用户信息', 'auth:me', '获取用户信息API权限', 'API', '/auth/me', 'GET', null, 35);

-- 插入基础角色数据
INSERT INTO roles (role_name, role_code, description, status) VALUES
('系统管理员', 'ADMIN', '拥有系统所有权限', 'ACTIVE'),
('管理员', 'MANAGER', '拥有管理权限', 'ACTIVE'),
('操作员', 'OPERATOR', '拥有操作权限', 'ACTIVE'),
('观察员', 'OBSERVER', '只读权限', 'ACTIVE');

-- 为系统管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 为管理员角色分配大部分权限（不包括删除操作）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE code NOT IN ('user:delete', 'role:delete', 'environment:delete', 'device:delete', 'production:delete');

-- 为操作员分配操作权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view', 'environment:create', 'environment:update',
    'device:manage', 'device:view', 'device:control',
    'production:manage', 'production:plan', 'production:view', 'production:create', 'production:update',
    'report:view', 'statistics:view',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 为观察员分配只读权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view',
    'device:manage', 'device:view',
    'production:manage', 'production:plan', 'production:view',
    'report:view', 'statistics:view',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 创建默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, real_name, status) VALUES
('admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'admin@plant-factory.com', '系统管理员', 'ACTIVE');

-- 为默认管理员分配系统管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 创建测试用户
INSERT INTO users (username, password, email, real_name, status) VALUES
('operator', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'operator@plant-factory.com', '操作员', 'ACTIVE'),
('observer', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'observer@plant-factory.com', '观察员', 'ACTIVE');

-- 为测试用户分配角色
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 3), -- operator -> OPERATOR
(3, 4); -- observer -> OBSERVER

COMMIT;