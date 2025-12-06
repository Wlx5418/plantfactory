# 植物工厂管理系统 - 项目结构说明

## 📁 项目目录结构

```
plantfactory/
├── 📂 database/                    # 数据库相关文件
│   ├── create_tables.sql            # 数据库表结构创建脚本
│   ├── database_init.sql           # 数据库初始化脚本
│   ├── insert_init_data.sql        # 初始数据插入脚本
│   ├── database_partition_strategy.sql     # 数据库分区策略
│   ├── database_partition_critical_fix.sql  # 数据库分区修复
│   └── environment_data_partition_fix.sql # 环境数据分区修复
│
├── 📂 docs/                        # 项目文档
│   ├── README.md                   # 项目说明文档
│   ├── CLAUDE.md                   # Claude Code 开发指南
│   ├── DEMO_GUIDE.md              # 演示指南
│   ├── PROJECT_SUMMARY.md          # 项目总结
│   ├── TEST_REPORT.md              # 测试报告
│   ├── INTEGRATION_TEST_SUMMARY.md # 集成测试总结
│   ├── API响应格式修复文档.md        # API修复文档
│   ├── 关键问题快速修复报告.md        # 问题修复报告
│   ├── 系统架构优化总结.md           # 架构优化文档
│   ├── 植物工厂管理系统-最终版.md     # 系统完整文档
│   ├── 植物工厂管理系统 - 前端技术文档（完整版 v2.1）.md # 前端技术文档
│   └── 植物工厂管理系统数据库设计-修复版.md               # 数据库设计文档
│
├── 📂 scripts/                     # 脚本文件
│   ├── automated_test.bat          # 自动化测试脚本（Windows）
│   ├── automated_test.sh          # 自动化测试脚本（Linux/Mac）
│   ├── run_tests.bat              # 运行测试脚本
│   └── start_and_test.bat        # 启动并测试脚本
│
├── 📂 tests/                       # 测试文件
│   ├── frontend_test.js           # 前端测试
│   ├── integration_test.js        # 集成测试
│   ├── websocket_test.js          # WebSocket测试
│   └── websocket_security_test.js # WebSocket安全测试
│
├── 📂 reports/                     # 测试报告
│   ├── integration-test-report.json # 集成测试报告
│   └── 植物工厂管理系统完整测试报告.txt # 完整测试报告
│
├── 📂 plantfactoryspring/          # Spring Boot 后端项目
│   ├── src/                       # 源代码目录
│   ├── pom.xml                    # Maven 配置文件
│   └── target/                    # 编译输出目录
│
├── 📂 plantfactoryvue/             # Vue.js 前端项目
│   ├── src/                       # 源代码目录
│   ├── package.json               # NPM 配置文件
│   ├── vite.config.js             # Vite 配置文件
│   └── node_modules/              # 依赖包目录
│
├── 📂 .claude/                     # Claude Code 配置
├── 📂 .git/                        # Git 版本控制
└── 📄 PROJECT_STRUCTURE.md         # 本文件
```

## 📋 文件说明

### 核心项目
- **plantfactoryspring/**: Spring Boot 后端服务，提供 REST API
- **plantfactoryvue/**: Vue.js 前端应用，提供用户界面

### 数据库
- **database/**: 包含所有数据库初始化、表结构、分区策略等 SQL 脚本

### 文档
- **docs/**: 包含项目设计文档、API文档、部署指南、测试报告等

### 脚本与测试
- **scripts/**: 自动化测试和部署脚本
- **tests/**: 各种功能和集成测试文件
- **reports/**: 测试执行结果和报告

## 🚀 快速启动

### 一键启动 (推荐)
```bash
# Windows用户
scripts/quick_start.bat

# Linux/Mac用户
scripts/quick_start.sh
```

### 手动启动
#### 后端启动
```bash
cd plantfactoryspring
./mvnw spring-boot:run
```

#### 前端启动
```bash
cd plantfactoryvue
npm install
npm run dev
```

## 🛠️ 服务管理

### 清理端口占用
```bash
# Windows用户
scripts/kill_ports.bat

# Linux/Mac用户
scripts/kill_ports.sh
```

### 检查服务状态
```bash
# Windows用户
scripts/check_status.bat
```

### 完整启动 (带端口清理)
```bash
# Windows用户
scripts/start_project.bat

# Linux/Mac用户
scripts/start_project.sh
```

## 📚 相关文档

- [项目README](docs/README.md)
- [开发指南](docs/CLAUDE.md)
- [演示指南](docs/DEMO_GUIDE.md)
- [API文档](docs/植物工厂管理系统-最终版.md)
- [数据库设计](docs/植物工厂管理系统数据库设计-修复版.md)

## 🛠️ 技术栈

- **后端**: Spring Boot 3.1.5, Spring Security, JPA, MySQL, Redis
- **前端**: Vue 3, Ant Design Vue, Vite, Pinia
- **数据库**: MySQL 8.0
- **认证**: JWT
- **构建工具**: Maven, Vite

---

*最后更新: 2025-11-27*