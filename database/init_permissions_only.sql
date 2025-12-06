-- 初始化权限数据
INSERT IGNORE INTO permissions (name, code, description, resource_type, resource_path, http_method, parent_id, sort_order, created_at, updated_at) VALUES
-- 用户管理权限
('用户管理', 'user_manage', '用户管理权限', 'MENU', '/user', NULL, NULL, 1, NOW(), NOW()),
('用户列表查看', 'user_view', '查看用户列表权限', 'API', '/api/users', 'GET', 1, 1, NOW(), NOW()),
('用户创建', 'user_create', '创建用户权限', 'API', '/api/users', 'POST', 1, 2, NOW(), NOW()),
('用户编辑', 'user_edit', '编辑用户权限', 'API', '/api/users/*', 'PUT', 1, 3, NOW(), NOW()),
('用户删除', 'user_delete', '删除用户权限', 'API', '/api/users/*', 'DELETE', 1, 4, NOW(), NOW()),
('用户状态管理', 'user_status', '用户状态管理权限', 'API', '/api/users/*/status', 'PUT', 1, 5, NOW(), NOW()),

-- 角色管理权限
('角色管理', 'role_manage', '角色管理权限', 'MENU', '/user/roles', NULL, NULL, 2, NOW(), NOW()),
('角色列表查看', 'role_view', '查看角色列表权限', 'API', '/api/roles', 'GET', 8, 1, NOW(), NOW()),
('角色创建', 'role_create', '创建角色权限', 'API', '/api/roles', 'POST', 8, 2, NOW(), NOW()),
('角色编辑', 'role_edit', '编辑角色权限', 'API', '/api/roles/*', 'PUT', 8, 3, NOW(), NOW()),
('角色删除', 'role_delete', '删除角色权限', 'API', '/api/roles/*', 'DELETE', 8, 4, NOW(), NOW()),
('角色权限管理', 'role_permission', '角色权限管理权限', 'API', '/api/roles/*/permissions', 'PUT', 8, 5, NOW(), NOW()),

-- 权限管理权限
('权限管理', 'permission_manage', '权限管理权限', 'MENU', '/system/permissions', NULL, NULL, 3, NOW(), NOW()),
('权限列表查看', 'permission_view', '查看权限列表权限', 'API', '/api/permissions', 'GET', 14, 1, NOW(), NOW()),
('权限创建', 'permission_create', '创建权限权限', 'API', '/api/permissions', 'POST', 14, 2, NOW(), NOW()),
('权限编辑', 'permission_edit', '编辑权限权限', 'API', '/api/permissions/*', 'PUT', 14, 3, NOW(), NOW()),
('权限删除', 'permission_delete', '删除权限权限', 'API', '/api/permissions/*', 'DELETE', 14, 4, NOW(), NOW()),

-- 环境监控权限
('环境监控', 'environment_monitor', '环境监控权限', 'MENU', '/environment', NULL, NULL, 4, NOW(), NOW()),
('环境数据查看', 'environment_view', '查看环境数据权限', 'API', '/api/environment/*', 'GET', 19, 1, NOW(), NOW()),
('环境历史数据', 'environment_history', '查看环境历史数据权限', 'API', '/api/environment/*/history', 'GET', 19, 2, NOW(), NOW()),

-- 设备控制权限
('设备控制', 'device_control', '设备控制权限', 'MENU', '/production/devices', NULL, NULL, 5, NOW(), NOW()),
('设备操作', 'device_operate', '操作设备权限', 'API', '/api/devices/*/control', 'POST', 22, 1, NOW(), NOW()),
('设备状态查看', 'device_status', '查看设备状态权限', 'API', '/api/devices/*/status', 'GET', 22, 2, NOW(), NOW()),

-- 数据分析权限
('数据查看', 'data_view', '数据查看权限', 'API', '/api/data/*', 'GET', NULL, 6, NOW(), NOW()),
('数据导出', 'data_export', '数据导出权限', 'API', '/api/data/export', 'POST', NULL, 7, NOW(), NOW()),

-- 系统配置权限
('系统配置', 'system_config', '系统配置权限', 'MENU', '/settings', NULL, NULL, 8, NOW(), NOW()),
('系统设置', 'system_settings', '系统设置权限', 'API', '/api/system/*', 'PUT', 26, 1, NOW(), NOW()),

-- 日志查看权限
('日志查看', 'log_view', '日志查看权限', 'API', '/api/logs/*', 'GET', NULL, 9, NOW(), NOW()),

-- 分区管理权限
('分区管理', 'area_manage', '分区管理权限', 'MENU', '/production/areas', NULL, NULL, 10, NOW(), NOW()),
('分区创建', 'area_create', '创建分区权限', 'API', '/api/areas', 'POST', 29, 1, NOW(), NOW()),
('分区编辑', 'area_edit', '编辑分区权限', 'API', '/api/areas/*', 'PUT', 29, 2, NOW(), NOW()),
('分区删除', 'area_delete', '删除分区权限', 'API', '/api/areas/*', 'DELETE', 29, 3, NOW(), NOW());