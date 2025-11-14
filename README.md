# 植物工厂管理系统

基于Spring Boot 3 + Vue 3的现代化智能农业管理系统，提供环境监控、生产区域管理、数据分析等功能。

## 🌱 项目简介

植物工厂管理系统是一个面向现代植物工厂的综合管理平台，集成了环境数据监控、生产区域管理、用户权限控制、数据分析等功能，帮助实现农业生产的数字化和智能化管理。

## ✨ 主要功能

### 🔐 用户管理
- 用户注册、登录、权限管理
- 基于角色的访问控制（RBAC）
- JWT令牌认证
- 用户状态管理

### 🏭 生产区域管理
- 生产区域创建、编辑、删除
- 区域容量和使用率监控
- 多种区域类型支持（生长区、育苗区、采收区等）
- 区域使用情况统计分析

### 🌡️ 环境监控
- 实时环境数据采集（温度、湿度、光照、CO2等）
- 历史数据查询和分析
- 异常数据告警
- 数据质量监控
- 批量数据导入导出

### 📊 数据分析
- 环境数据趋势分析
- 统计报表生成
- 可视化图表展示
- 数据导出功能

### ⚙️ 系统管理
- 系统参数配置
- 日志管理
- 数据备份恢复

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 3.1.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **文档**: Swagger/OpenAPI 3
- **构建工具**: Maven

### 前端
- **框架**: Vue 3.5.x
- **UI库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **图表**: ECharts
- **构建工具**: Vite
- **TypeScript**: 支持

### 部署
- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx
- **SSL**: HTTPS支持
- **监控**: Actuator健康检查

## 📦 快速开始

### 环境要求

- Java 17+
- Node.js 20+
- MySQL 8.0+
- Redis 6.0+
- Docker & Docker Compose (可选)

### 开发环境部署

#### 1. 克隆项目
```bash
git clone https://github.com/your-username/plant-factory-management.git
cd plant-factory-management
```

#### 2. 后端启动
```bash
cd plantfactoryspring

# 配置数据库连接（修改application.yml）
# 启动后端服务
./mvnw spring-boot:run
```

#### 3. 前端启动
```bash
cd plantfactoryvue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### Docker部署

#### 1. 构建项目
```bash
# Linux/Mac
./scripts/build.sh

# Windows
scripts\build.bat
```

#### 2. 启动服务
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

#### 3. 初始化数据
```bash
# 自动创建数据库和默认用户
# 默认管理员账号: admin / 123456
```

### 访问地址

- **前端地址**: http://localhost
- **后端API**: http://localhost/api
- **API文档**: http://localhost/api/swagger-ui.html

## 📁 项目结构

```
plant-factory-management/
├── plantfactoryspring/          # Spring Boot后端
│   ├── src/main/java/          # Java源代码
│   │   └── com/qzsf/plantfactory/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # 实体类
│   │       ├── repository/    # 数据访问层
│   │       ├── security/      # 安全配置
│   │       └── service/       # 业务逻辑层
│   ├── src/main/resources/     # 配置文件
│   └── pom.xml                # Maven配置
├── plantfactoryvue/            # Vue 3前端
│   ├── src/                   # 源代码
│   │   ├── api/              # API接口
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── layout/           # 布局组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # 状态管理
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面组件
│   ├── public/               # 公共文件
│   ├── package.json          # 依赖配置
│   └── vite.config.js        # Vite配置
├── docker/                    # Docker配置
│   ├── mysql/               # MySQL配置
│   ├── nginx/               # Nginx配置
│   └── redis/               # Redis配置
├── scripts/                   # 构建脚本
├── docker-compose.yml         # Docker编排
├── Dockerfile                 # Docker镜像
└── README.md                  # 项目说明
```

## 🔧 配置说明

### 数据库配置

修改 `plantfactoryspring/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plant_factory?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: plant_factory
    password: your_password
```

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### JWT配置

```yaml
app:
  jwt:
    secret: your-jwt-secret-key
    expiration-in-ms: 86400000  # 24小时
```

## 🚀 API文档

系统集成了Swagger文档，启动后端服务后可访问：

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

### 主要API端点

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/refresh` - 刷新令牌

#### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

#### 生产区域
- `GET /api/production-areas` - 获取生产区域列表
- `POST /api/production-areas` - 创建生产区域
- `PUT /api/production-areas/{id}` - 更新生产区域
- `GET /api/production-areas/{id}/usage` - 获取区域使用情况

#### 环境数据
- `GET /api/environment-data` - 获取环境数据列表
- `POST /api/environment-data` - 记录环境数据
- `GET /api/environment-data/area/{id}/latest` - 获取最新数据
- `GET /api/environment-data/area/{id}/range` - 获取时间范围数据

## 🧪 测试

### 后端测试
```bash
cd plantfactoryspring

# 运行单元测试
./mvnw test

# 运行集成测试
./mvnw verify
```

### 前端测试
```bash
cd plantfactoryvue

# 运行单元测试
npm run test

# 运行端到端测试
npm run test:e2e
```

## 📈 监控和日志

### 应用监控
- **健康检查**: `/actuator/health`
- **应用信息**: `/actuator/info`
- **指标**: `/actuator/metrics`

### 日志配置
日志级别可在 `application.yml` 中配置：

```yaml
logging:
  level:
    com.qzsf: DEBUG
    org.springframework.security: DEBUG
    root: INFO
```

## 🔒 安全性

- JWT令牌认证
- 密码BCrypt加密
- CORS跨域保护
- SQL注入防护
- XSS攻击防护
- CSRF保护

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: Plant Factory Team
- 邮箱: support@plantfactory.com
- 问题反馈: [GitHub Issues](https://github.com/your-username/plant-factory-management/issues)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！