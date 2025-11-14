-- 创建数据库和用户
CREATE DATABASE IF NOT EXISTS plant_factory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'plant_factory'@'%' IDENTIFIED BY 'plant_factory_2024';
GRANT ALL PRIVILEGES ON plant_factory.* TO 'plant_factory'@'%';
FLUSH PRIVILEGES;

-- 使用数据库
USE plant_factory;

-- 设置时区
SET GLOBAL time_zone = '+08:00';

-- 显示创建的数据库
SHOW DATABASES;