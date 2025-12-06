-- H2数据库初始化脚本（植物工厂管理系统认证模块）

-- 1. 权限表 (permissions)
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource_type VARCHAR(20) DEFAULT 'API',
    resource_path VARCHAR(500),
    http_method VARCHAR(20),
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 角色表 (roles)
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 角色权限关联表 (role_permissions)
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 用户表 (users)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 5. 用户角色关联表 (user_roles)
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建唯一约束
CREATE UNIQUE INDEX uk_role_permission ON role_permissions(role_id, permission_id);
CREATE UNIQUE INDEX uk_user_role ON user_roles(user_id, role_id);