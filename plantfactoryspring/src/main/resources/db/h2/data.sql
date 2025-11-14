-- H2数据库初始数据

-- 插入基础权限数据
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
    'environment:monitor', 'environment:data', 'environment:view', 'environment:create',
    'device:manage', 'device:view', 'device:control',
    'report:view', 'statistics:view',
    'auth:login', 'auth:logout', 'auth:refresh', 'auth:me'
);

-- 为观察员分配只读权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE code IN (
    'environment:monitor', 'environment:data', 'environment:view',
    'device:manage', 'device:view',
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