<template>
  <div class="logs-page">
    <page-header title="系统日志" subtitle="查看系统运行日志和操作记录" />

    <div class="content-container">
      <!-- 筛选条件 -->
      <a-card class="filter-card" :bordered="false">
        <a-form
          :model="searchForm"
          layout="inline"
          @finish="handleSearch"
        >
          <a-form-item label="日志级别">
            <a-select
              v-model:value="searchForm.level"
              placeholder="选择日志级别"
              style="width: 120px"
              allow-clear
            >
              <a-select-option value="INFO">信息</a-select-option>
              <a-select-option value="WARN">警告</a-select-option>
              <a-select-option value="ERROR">错误</a-select-option>
              <a-select-option value="DEBUG">调试</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="操作类型">
            <a-select
              v-model:value="searchForm.operation"
              placeholder="选择操作类型"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="login">登录</a-select-option>
              <a-select-option value="logout">登出</a-select-option>
              <a-select-option value="create">创建</a-select-option>
              <a-select-option value="update">更新</a-select-option>
              <a-select-option value="delete">删除</a-select-option>
              <a-select-option value="control">设备控制</a-select-option>
              <a-select-option value="export">数据导出</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="用户">
            <a-select
              v-model:value="searchForm.userId"
              placeholder="选择用户"
              style="width: 150px"
              allow-clear
              show-search
              :filter-option="filterOption"
            >
              <a-select-option
                v-for="user in userList"
                :key="user.id"
                :value="user.id"
              >
                {{ user.name }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="loading">
                <search-outlined />
                查询
              </a-button>
              <a-button @click="handleReset">重置</a-button>
              <a-button @click="handleExport">
                <export-outlined />
                导出日志
              </a-button>
              <a-button @click="handleClear" danger>
                <delete-outlined />
                清理日志
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 日志统计 -->
      <a-row :gutter="16" class="stats-row">
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="今日日志"
              :value="stats.todayCount"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="错误日志"
              :value="stats.errorCount"
              :value-style="{ color: '#ff4d4f' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="警告日志"
              :value="stats.warningCount"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="总日志数"
              :value="stats.totalCount"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 日志列表 -->
      <a-card class="table-card" :bordered="false">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
          :scroll="{ x: 1400 }"
          :row-class-name="getRowClassName"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'level'">
              <a-tag :color="getLevelColor(record.level)">
                {{ getLevelText(record.level) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'operation'">
              <a-tag :color="getOperationColor(record.operation)">
                {{ getOperationText(record.operation) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'ip'">
              <a-tooltip :title="record.location">
                {{ record.ip }}
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'message'">
              <a-tooltip :title="record.message">
                <div class="message-cell">
                  {{ record.message }}
                </div>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="viewDetails(record)">
                  详情
                </a-button>
                <a-popconfirm
                  v-if="record.level === 'ERROR'"
                  title="确定要处理这条错误日志吗？"
                  @confirm="handleError(record.id)"
                  ok-text="确定"
                  cancel-text="取消"
                >
                  <a-button type="link" size="small" color="red">
                    处理
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 日志详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="日志详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions v-if="currentLog" :column="2" bordered>
        <a-descriptions-item label="日志ID" :span="2">
          {{ currentLog.id }}
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ currentLog.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="用户ID">
          {{ currentLog.userId }}
        </a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOperationColor(currentLog.operation)">
            {{ getOperationText(currentLog.operation) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="日志级别">
          <a-tag :color="getLevelColor(currentLog.level)">
            {{ getLevelText(currentLog.level) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">
          {{ currentLog.ip }}
        </a-descriptions-item>
        <a-descriptions-item label="地理位置">
          {{ currentLog.location }}
        </a-descriptions-item>
        <a-descriptions-item label="设备信息">
          {{ currentLog.userAgent }}
        </a-descriptions-item>
        <a-descriptions-item label="操作时间" :span="2">
          {{ currentLog.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="操作结果" :span="2">
          <a-tag :color="currentLog.success ? 'green' : 'red'">
            {{ currentLog.success ? '成功' : '失败' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="详细信息" :span="2">
          <div class="log-message">
            {{ currentLog.message }}
          </div>
        </a-descriptions-item>
        <a-descriptions-item v-if="currentLog.details" label="附加信息" :span="2">
          <pre class="log-details">{{ currentLog.details }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined, ExportOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

// 数据状态
const loading = ref(false)
const dataSource = ref([])
const detailModalVisible = ref(false)
const currentLog = ref(null)

// 搜索表单
const searchForm = reactive({
  level: undefined,
  operation: undefined,
  userId: undefined,
  dateRange: []
})

// 统计数据
const stats = reactive({
  todayCount: 0,
  errorCount: 0,
  warningCount: 0,
  totalCount: 0
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 用户列表
const userList = ref([
  { id: 1, name: 'admin' },
  { id: 2, name: 'operator' },
  { id: 3, name: 'observer' },
  { id: 4, name: 'manager' }
])

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '级别',
    key: 'level',
    width: 80
  },
  {
    title: '操作',
    key: 'operation',
    width: 100
  },
  {
    title: '用户',
    dataIndex: 'userName',
    key: 'userName',
    width: 100
  },
  {
    title: 'IP地址',
    key: 'ip',
    width: 130
  },
  {
    title: '消息内容',
    key: 'message',
    ellipsis: true
  },
  {
    title: '结果',
    key: 'success',
    width: 80,
    render: (success) => (
      success ?
      <a-tag color="green">成功</a-tag> :
      <a-tag color="red">失败</a-tag>
    )
  },
  {
    title: '时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 120
  }
]

// 获取级别颜色
const getLevelColor = (level) => {
  const colors = {
    INFO: 'blue',
    WARN: 'orange',
    ERROR: 'red',
    DEBUG: 'default'
  }
  return colors[level] || 'default'
}

// 获取级别文本
const getLevelText = (level) => {
  const texts = {
    INFO: '信息',
    WARN: '警告',
    ERROR: '错误',
    DEBUG: '调试'
  }
  return texts[level] || level
}

// 获取操作颜色
const getOperationColor = (operation) => {
  const colors = {
    login: 'green',
    logout: 'gray',
    create: 'blue',
    update: 'orange',
    delete: 'red',
    control: 'purple',
    export: 'cyan'
  }
  return colors[operation] || 'default'
}

// 获取操作文本
const getOperationText = (operation) => {
  const texts = {
    login: '登录',
    logout: '登出',
    create: '创建',
    update: '更新',
    delete: '删除',
    control: '设备控制',
    export: '数据导出'
  }
  return texts[operation] || operation
}

// 获取行样式
const getRowClassName = (record) => {
  if (record.level === 'ERROR') {
    return 'error-row'
  } else if (record.level === 'WARN') {
    return 'warning-row'
  }
  return ''
}

// 用户搜索过滤
const filterOption = (input, option) => {
  return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 生成模拟数据
const generateMockData = () => {
  const levels = ['INFO', 'WARN', 'ERROR', 'DEBUG']
  const operations = ['login', 'logout', 'create', 'update', 'delete', 'control', 'export']
  const users = ['admin', 'operator', 'observer', 'manager']
  const ips = ['192.168.1.100', '192.168.1.101', '192.168.1.102', '10.0.0.100']
  const messages = [
    '用户登录系统',
    '用户登出系统',
    '创建新的环境监控记录',
    '更新设备配置参数',
    '删除过期数据记录',
    '远程控制灌溉系统',
    '导出环境数据报告',
    '修改用户权限配置',
    '系统备份操作',
    '设备异常告警'
  ]

  const data = []
  for (let i = 1; i <= 100; i++) {
    const level = levels[Math.floor(Math.random() * levels.length)]
    const success = Math.random() > 0.2

    data.push({
      id: i,
      level,
      operation: operations[Math.floor(Math.random() * operations.length)],
      userName: users[Math.floor(Math.random() * users.length)],
      userId: Math.floor(Math.random() * 4) + 1,
      ip: ips[Math.floor(Math.random() * ips.length)],
      location: '本地网络',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
      message: messages[Math.floor(Math.random() * messages.length)],
      success,
      createTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString(),
      details: level === 'ERROR' ? 'Error stack trace...\n  at Function.invoke (app.js:123)\n  at Object.process (handler.js:456)' : null
    })
  }

  return data.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
}

// 加载日志数据
const loadLogs = () => {
  loading.value = true

  setTimeout(() => {
    const data = generateMockData()
    dataSource.value = data
    pagination.total = data.length

    // 更新统计数据
    stats.todayCount = data.filter(log => {
      const logDate = new Date(log.createTime)
      const today = new Date()
      return logDate.toDateString() === today.toDateString()
    }).length
    stats.errorCount = data.filter(log => log.level === 'ERROR').length
    stats.warningCount = data.filter(log => log.level === 'WARN').length
    stats.totalCount = data.length

    loading.value = false
  }, 1000)
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  loadLogs()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    level: undefined,
    operation: undefined,
    userId: undefined,
    dateRange: []
  })
  handleSearch()
}

// 导出日志
const handleExport = () => {
  message.info('日志导出功能开发中...')
}

// 清理日志
const handleClear = () => {
  Modal.confirm({
    title: '确认清理日志',
    content: '确定要清理30天前的历史日志吗？此操作不可恢复。',
    okText: '确定',
    cancelText: '取消',
    onOk() {
      message.success('日志清理成功')
      loadLogs()
    }
  })
}

// 查看详情
const viewDetails = (record) => {
  currentLog.value = record
  detailModalVisible.value = true
}

// 处理错误
const handleError = (logId) => {
  message.success('错误日志已标记为已处理')
  loadLogs()
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadLogs()
}

// 组件挂载
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.logs-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-message {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.log-details {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  font-size: 12px;
  color: #666;
}

:deep(.error-row) {
  background-color: #fff2f0;
}

:deep(.warning-row) {
  background-color: #fffbe6;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }
}
</style>