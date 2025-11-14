# API响应格式修复文档

## 修复概述

本文档修复了原API文档中响应格式不一致的问题，统一了所有API接口的响应格式，确保前端可以统一处理响应数据。

---

## 1. 统一响应格式规范

### 1.1 成功响应格式
所有API成功响应都遵循统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据，具体内容由各个接口定义
  },
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 100,
    "pages": 5,
    "hasPrevious": false,
    "hasNext": true
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_123456789",
  "executionTime": 45
}
```

### 1.2 错误响应格式
所有API错误响应都遵循统一格式：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": {
    "type": "VALIDATION_ERROR",
    "code": "INVALID_PARAMETER",
    "details": [
      {
        "field": "username",
        "message": "用户名不能为空",
        "rejectedValue": ""
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_987654321"
}
```

---

## 2. 修复后的接口示例

### 2.1 用户管理接口

#### 用户登录
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "captcha": "ABCD"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@plant-factory.com",
      "status": "ACTIVE",
      "roles": [
        {
          "id": 1,
          "roleName": "系统管理员",
          "roleCode": "ADMIN"
        }
      ],
      "permissions": ["user:create", "user:read", "user:update", "user:delete"]
    }
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_login_123",
  "executionTime": 120
}
```

#### 获取用户列表
```http
GET /users?page=1&size=20&status=ACTIVE
Authorization: Bearer JWT_TOKEN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@plant-factory.com",
      "phone": "13800138000",
      "status": "ACTIVE",
      "lastLoginTime": "2024-01-01T10:30:00Z",
      "createdAt": "2024-01-01T08:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z",
      "roles": [
        {
          "id": 1,
          "roleName": "系统管理员",
          "roleCode": "ADMIN"
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 50,
    "pages": 3,
    "hasPrevious": false,
    "hasNext": true
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_user_list_456",
  "executionTime": 35
}
```

### 2.2 环境数据接口

#### 获取环境数据列表
```http
GET /environment/data?areaId=1&startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z&page=1&size=20
Authorization: Bearer JWT_TOKEN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "areaId": 1,
      "areaName": "A区-育苗",
      "dataTime": "2024-01-01T10:00:00Z",
      "temperature": 25.5,
      "humidity": 65.0,
      "lightIntensity": 8000,
      "co2Level": 1200,
      "phValue": 6.8,
      "ecValue": 1.8,
      "soilMoisture": 70.0,
      "dataSource": "MANUAL",
      "qualityFlag": "GOOD",
      "operatorId": 1,
      "operatorName": "系统管理员",
      "createdAt": "2024-01-01T10:01:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 1000,
    "pages": 50,
    "hasPrevious": false,
    "hasNext": true
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_env_data_789",
  "executionTime": 85
}
```

#### 批量导入环境数据
```http
POST /environment/data/batch-import
Authorization: Bearer JWT_TOKEN
Content-Type: multipart/form-data

file: environment_data.xlsx
areaId: 1
dataSource: EXCEL_IMPORT
```

**响应示例：**
```json
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "taskId": "import_task_123456",
    "totalRows": 1000,
    "successCount": 980,
    "failedCount": 20,
    "failedRecords": [
      {
        "rowNumber": 15,
        "errorMessage": "温度值超出范围",
        "field": "temperature",
        "value": 50.5
      }
    ],
    "importTime": "2024-01-01T12:05:00Z"
  },
  "timestamp": "2024-01-01T12:05:00Z",
  "requestId": "req_batch_import_321",
  "executionTime": 5200
}
```

### 2.3 设备配置接口

#### 获取设备列表
```http
GET /devices?deviceType=MONITOR&areaId=1&page=1&size=20
Authorization: Bearer JWT_TOKEN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "deviceCode": "ENV_001",
      "deviceName": "环境监测设备A",
      "deviceType": "MONITOR",
      "deviceCategory": "temperature_humidity_sensor",
      "areaId": 1,
      "areaName": "A区-育苗",
      "location": "A区中央",
      "manufacturer": "Tech Company",
      "model": "TH-1000",
      "parameters": {
        "temperature_range": [18, 28],
        "humidity_threshold": [60, 80],
        "data_import_config": {
          "excel_template": "env_data_v1.0",
          "required_fields": ["temperature", "humidity"]
        }
      },
      "status": "NORMAL",
      "dataSource": "MANUAL",
      "operatorId": 1,
      "operatorName": "系统管理员",
      "lastMaintenanceDate": "2024-01-01",
      "createdAt": "2024-01-01T08:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 10,
    "pages": 1,
    "hasPrevious": false,
    "hasNext": false
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_device_list_654",
  "executionTime": 45
}
```

### 2.4 生产计划接口

#### 获取生产计划列表
```http
GET /production/plans?plantType=lettuce&status=IN_PROGRESS&page=1&size=20
Authorization: Bearer JWT_TOKEN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "planName": "2024年第一季度生菜种植计划",
      "planCode": "PLAN_2024_Q1_LETTUCE_001",
      "plantType": "lettuce",
      "variety": "罗马生菜",
      "areaId": 1,
      "areaName": "A区-育苗",
      "totalArea": 100.0,
      "plantingDensity": 25,
      "expectedYield": 350.0,
      "growthCycle": 45,
      "plantingDate": "2024-01-01",
      "expectedHarvestDate": "2024-02-15",
      "actualHarvestDate": null,
      "planStatus": "IN_PROGRESS",
      "creatorId": 1,
      "creatorName": "系统管理员",
      "remarks": "优质品种种植计划",
      "createdAt": "2024-01-01T08:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 5,
    "pages": 1,
    "hasPrevious": false,
    "hasNext": false
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_plan_list_987",
  "executionTime": 28
}
```

#### 获取计划执行状态
```http
GET /production/plans/1/execution-status
Authorization: Bearer JWT_TOKEN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "planId": 1,
    "planName": "2024年第一季度生菜种植计划",
    "planStatus": "IN_PROGRESS",
    "totalBatches": 4,
    "completedBatches": 1,
    "inProgressBatches": 2,
    "plannedBatches": 1,
    "completionRate": 0.25,
    "totalExpectedYield": 350.0,
    "totalActualYield": 95.5,
    "yieldCompletionRate": 0.27,
    "executionProgress": [
      {
        "batchId": 1,
        "batchCode": "LETTUCE_202401_001",
        "batchStatus": "COMPLETED",
        "actualYield": 95.5,
        "expectedYield": 87.5,
        "completionRate": 1.0,
        "harvestDate": "2024-02-15"
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_plan_status_654",
  "executionTime": 62
}
```

---

## 3. 错误响应标准化

### 3.1 参数验证错误
```http
POST /users
Authorization: Bearer JWT_TOKEN
Content-Type: application/json

{
  "username": "",
  "email": "invalid-email"
}
```

**响应示例：**
```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": {
    "type": "VALIDATION_ERROR",
    "code": "INVALID_PARAMETER",
    "details": [
      {
        "field": "username",
        "message": "用户名不能为空",
        "rejectedValue": ""
      },
      {
        "field": "email",
        "message": "邮箱格式不正确",
        "rejectedValue": "invalid-email"
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_validation_111"
}
```

### 3.2 业务逻辑错误
```http
POST /devices
Authorization: Bearer JWT_TOKEN
Content-Type: application/json

{
  "deviceCode": "ENV_001", // 已存在的设备编码
  "deviceName": "新设备",
  "deviceType": "MONITOR"
}
```

**响应示例：**
```json
{
  "code": 20002,
  "message": "设备编码已存在",
  "error": {
    "type": "BUSINESS_ERROR",
    "code": "DEVICE_CODE_EXISTS",
    "details": "设备编码 'ENV_001' 已存在，请使用其他编码"
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_device_exists_222"
}
```

### 3.3 权限错误
```http
DELETE /users/1
Authorization: Bearer INVALID_TOKEN
```

**响应示例：**
```json
{
  "code": 403,
  "message": "权限不足",
  "error": {
    "type": "PERMISSION_ERROR",
    "code": "ACCESS_DENIED",
    "details": "当前用户没有执行该操作的权限"
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_permission_333"
}
```

### 3.4 认证错误
```http
GET /users
Authorization: Bearer EXPIRED_TOKEN
```

**响应示例：**
```json
{
  "code": 401,
  "message": "Token已过期",
  "error": {
    "type": "AUTH_ERROR",
    "code": "TOKEN_EXPIRED",
    "details": "访问令牌已过期，请重新登录"
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "req_auth_444"
}
```

---

## 4. HTTP状态码规范

### 4.1 成功状态码
- **200 OK**: 请求成功
- **201 Created**: 资源创建成功
- **204 No Content**: 删除成功，无返回内容

### 4.2 客户端错误状态码
- **400 Bad Request**: 请求参数错误
- **401 Unauthorized**: 未授权访问
- **403 Forbidden**: 权限不足
- **404 Not Found**: 资源不存在
- **422 Unprocessable Entity**: 数据验证失败
- **429 Too Many Requests**: 请求频率超限

### 4.3 服务器错误状态码
- **500 Internal Server Error**: 服务器内部错误
- **503 Service Unavailable**: 服务不可用

---

## 5. 响应头规范

### 5.1 标准响应头
```http
Content-Type: application/json;charset=UTF-8
X-Request-ID: req_123456789
X-Execution-Time: 45
Cache-Control: no-cache, no-store, must-revalidate
```

### 5.2 分页响应头（可选）
```http
X-Total-Count: 100
X-Page-Count: 5
X-Current-Page: 1
X-Per-Page: 20
```

---

## 6. 前端处理建议

### 6.1 响应拦截器示例
```javascript
// API响应拦截器
apiClient.interceptors.response.use(
  response => {
    const { data } = response;

    // 检查业务状态码
    if (data.code !== 200 && data.code !== 201) {
      return Promise.reject(new Error(data.message));
    }

    return response;
  },
  error => {
    const { response } = error;

    if (response) {
      const { data } = response;

      // 处理业务错误
      if (data.error) {
        switch (data.error.type) {
          case 'VALIDATION_ERROR':
            // 处理参数验证错误
            showValidationErrors(data.error.details);
            break;
          case 'AUTH_ERROR':
            // 处理认证错误
            redirectToLogin();
            break;
          case 'PERMISSION_ERROR':
            // 处理权限错误
            showPermissionError();
            break;
          default:
            // 其他业务错误
            showError(data.message);
        }
      } else {
        // 处理HTTP错误
        showHttpError(response.status, response.statusText);
      }
    }

    return Promise.reject(error);
  }
);
```

### 6.2 错误处理函数
```javascript
// 显示验证错误
function showValidationErrors(details) {
  details.forEach(error => {
    const fieldElement = document.querySelector(`[name="${error.field}"]`);
    if (fieldElement) {
      fieldElement.setCustomValidity(error.message);
      fieldElement.reportValidity();
    }
  });
}

// 显示业务错误
function showError(message) {
  message.error(message || '操作失败，请重试');
}

// 显示权限错误
function showPermissionError() {
  message.error('权限不足，请联系管理员');
}

// 重定向到登录页
function redirectToLogin() {
  message.error('登录已过期，请重新登录');
  window.location.href = '/login';
}
```

---

## 7. 修复要点总结

### 7.1 统一响应结构
- 所有接口响应都包含 `code`、`message`、`data`、`timestamp`、`requestId`
- 分页接口包含 `pagination` 对象
- 错误响应包含 `error` 对象

### 7.2 标准化错误处理
- 统一错误类型定义
- 详细的错误信息提供
- 标准化的HTTP状态码使用

### 7.3 增强的追踪能力
- 每个响应包含唯一的 `requestId`
- 记录请求执行时间 `executionTime`
- 标准化的响应时间戳

### 7.4 前端友好设计
- 清晰的错误信息结构
- 便于前端统一处理
- 支持批量错误信息展示

---

*修复版本: v2.1*
*修复日期: 2025年11月*
*维护团队: 植物工厂管理系统API团队*