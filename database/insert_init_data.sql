-- 植物工厂管理系统 - 初始化数据脚本

-- 使用数据库
USE plant_factory;

-- 初始化权限数据
INSERT INTO permissions (name, code, description, resource_type, sort_order) VALUES
('System Manage', 'system:manage', 'System management permissions', 'MENU', 1),
('User Manage', 'user:manage', 'User management permissions', 'MENU', 2),
('User View', 'user:view', 'View user list permissions', 'BUTTON', 3),
('User Create', 'user:create', 'Create user permissions', 'BUTTON', 4),
('User Update', 'user:update', 'Update user permissions', 'BUTTON', 5),
('User Delete', 'user:delete', 'Delete user permissions', 'BUTTON', 6),
('Environment Monitor', 'environment:monitor', 'Environment monitoring permissions', 'MENU', 7),
('Environment Data', 'environment:data', 'Environment data management permissions', 'MENU', 8),
('Environment View', 'environment:view', 'View environment data permissions', 'BUTTON', 9),
('Environment Create', 'environment:create', 'Input environment data permissions', 'BUTTON', 10),
('Device Manage', 'device:manage', 'Device management permissions', 'MENU', 11),
('Device View', 'device:view', 'View device list permissions', 'BUTTON', 12),
('Device Control', 'device:control', 'Device control permissions', 'BUTTON', 13),
('Auth Login', 'auth:login', 'User login API permissions', 'API', 14),
('Auth Logout', 'auth:logout', 'User logout API permissions', 'API', 15),
('Auth Refresh', 'auth:refresh', 'Refresh token API permissions', 'API', 16),
('Auth Me', 'auth:me', 'Get user info API permissions', 'API', 17);

-- 初始化角色数据
INSERT INTO roles (role_name, role_code, description, status) VALUES
('Administrator', 'ADMIN', 'System administrator with all permissions', 'ACTIVE'),
('Manager', 'MANAGER', 'Manager with management permissions', 'ACTIVE'),
('Operator', 'OPERATOR', 'Operator with operation permissions', 'ACTIVE'),
('Observer', 'OBSERVER', 'Observer with read-only permissions', 'ACTIVE');

-- 为系统管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 为管理员角色分配大部分权限（不包括删除操作）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE code NOT IN ('user:delete', 'role:delete', 'environment:delete', 'device:delete');

-- 为操作员分配操作权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view', 'environment:create',
    'device:manage', 'device:view', 'device:control',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 为观察员分配只读权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view',
    'device:manage', 'device:view',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 创建默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, real_name, status) VALUES
('admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'admin@plantfactory.com', 'System Administrator', 'ACTIVE');

-- 为默认管理员分配系统管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 创建测试用户
INSERT INTO users (username, password, email, real_name, status) VALUES
('operator', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'operator@plantfactory.com', 'Operator', 'ACTIVE'),
('observer', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8e', 'observer@plantfactory.com', 'Observer', 'ACTIVE');

-- 为测试用户分配角色
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 3), -- operator -> OPERATOR
(3, 4); -- observer -> OBSERVER

-- 初始化生产区域
INSERT INTO production_areas (area_code, area_name, area_type, capacity, location) VALUES
('AREA001', 'Seedling Area', 'SEEDING', 50.00, 'East Floor 1'),
('AREA002', 'Growing Area A', 'GROWING', 200.00, 'East Floor 2'),
('AREA003', 'Growing Area B', 'GROWING', 150.00, 'East Floor 3'),
('AREA004', 'Harvest Area', 'HARVESTING', 100.00, 'West Floor 1');