<template>
  <div class="devices-page">
    <page-header title="设备管理" subtitle="管理植物工厂的智能设备和控制系统" />

    <div class="content-container">
      <!-- 操作区域 -->
      <a-card class="action-card" :bordered="false">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <plus-outlined />
            添加设备
          </a-button>
          <a-button @click="handleBatchControl" :disabled="!hasSelected">
            <control-outlined />
            批量控制
          </a-button>
          <a-button @click="handleRefresh">
            <reload-outlined />
            刷新
          </a-button>
        </a-space>

        <div class="selected-info" v-if="hasSelected">
          已选择 {{ selectedRowKeys.length }} 个设备
        </div>
      </a-card>

      <!-- 设备统计 -->
      <a-row :gutter="16" class="stats-row">
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="总设备数"
              :value="stats.totalDevices"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="在线设备"
              :value="stats.onlineDevices"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="运行中"
              :value="stats.runningDevices"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="故障设备"
              :value="stats.faultDevices"
              :value-style="{ color: '#ff4d4f' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 设备列表 -->
      <a-card class="table-card" :bordered="false">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :row-selection="rowSelection"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-space direction="vertical" size="small">
                <a-tag :color="getOnlineColor(record.online)">
                  {{ record.online ? '在线' : '离线' }}
                </a-tag>
                <a-switch
                  :checked="record.running"
                  :disabled="!record.online"
                  @change="(checked) => toggleDevice(record.id, checked)"
                  :loading="record.loading"
                />
              </a-space>
            </template>
            <template v-else-if="column.key === 'type'">
              <a-tag :color="getTypeColor(record.type)">
                {{ getTypeText(record.type) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'area'">
              <a-tag color="blue">{{ record.areaName }}</a-tag>
            </template>
            <template v-else-if="column.key === 'parameters'">
              <div class="parameters-cell">
                <div v-for="param in record.parameters.slice(0, 2)" :key="param.name" class="param-item">
                  <span class="param-name">{{ param.name }}:</span>
                  <span class="param-value">{{ param.value }}{{ param.unit }}</span>
                </div>
                <a-tooltip v-if="record.parameters.length > 2" :title="getParametersTooltip(record.parameters)">
                  <a-tag size="small">+{{ record.parameters.length - 2 }}</a-tag>
                </a-tooltip>
              </div>
            </template>
            <template v-else-if="column.key === 'schedule'">
              <a-tag v-if="record.hasSchedule" color="green">
                <clock-circle-outlined />
                已配置
              </a-tag>
              <a-tag v-else color="default">
                <clock-circle-outlined />
                未配置
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="controlDevice(record)">
                  控制
                </a-button>
                <a-button type="link" size="small" @click="viewDetails(record)">
                  详情
                </a-button>
                <a-button type="link" size="small" @click="scheduleDevice(record)">
                  定时
                </a-button>
                <a-dropdown>
                  <a-button type="link" size="small">
                    <more-outlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="editDevice(record)">
                        <edit-outlined /> 编辑
                      </a-menu-item>
                      <a-menu-item @click="maintainDevice(record)">
                        <tool-outlined /> 维护
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item @click="deleteDevice(record)" danger>
                        <delete-outlined /> 删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 创建/编辑设备弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑设备' : '添加设备'"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="设备名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入设备名称" />
        </a-form-item>

        <a-form-item label="设备类型" name="type">
          <a-select v-model:value="form.type" placeholder="请选择设备类型">
            <a-select-option value="fan">通风设备</a-select-option>
            <a-select-option value="light">照明设备</a-select-option>
            <a-select-option value="pump">水泵设备</a-select-option>
            <a-select-option value="heater">加热设备</a-select-option>
            <a-select-option value="cooler">制冷设备</a-select-option>
            <a-select-option value="sensor">传感器</a-select-option>
            <a-select-option value="controller">控制器</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="设备编号" name="code">
          <a-input v-model:value="form.code" placeholder="请输入设备编号" />
        </a-form-item>

        <a-form-item label="所属分区" name="areaId">
          <a-select v-model:value="form.areaId" placeholder="请选择所属分区">
            <a-select-option value="1">A区</a-select-option>
            <a-select-option value="2">B区</a-select-option>
            <a-select-option value="3">C区</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="安装位置" name="location">
          <a-input v-model:value="form.location" placeholder="请输入安装位置" />
        </a-form-item>

        <a-form-item label="设备描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入设备描述"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 设备控制弹窗 -->
    <a-modal
      v-model:open="controlModalVisible"
      title="设备控制"
      width="500px"
      :footer="null"
    >
      <div v-if="currentDevice" class="device-control">
        <div class="device-info">
          <h4>{{ currentDevice.name }}</h4>
          <a-tag :color="getOnlineColor(currentDevice.online)">
            {{ currentDevice.online ? '在线' : '离线' }}
          </a-tag>
        </div>

        <a-divider />

        <div class="control-panel">
          <div class="control-item">
            <label>设备开关:</label>
            <a-switch
              :checked="currentDevice.running"
              :disabled="!currentDevice.online"
              @change="(checked) => toggleDevice(currentDevice.id, checked)"
            />
          </div>

          <div v-for="param in currentDevice.parameters" :key="param.name" class="control-item">
            <label>{{ param.name }}:</label>
            <a-slider
              v-model:value="param.value"
              :min="param.min"
              :max="param.max"
              :disabled="!currentDevice.online || !currentDevice.running"
              @change="updateDeviceParam(currentDevice.id, param.name, $event)"
            />
            <span class="param-display">{{ param.value }}{{ param.unit }}</span>
          </div>
        </div>

        <a-divider />

        <div class="quick-actions">
          <a-space>
            <a-button type="primary" @click="applyControlSettings">
              应用设置
            </a-button>
            <a-button @click="resetControlSettings">
              重置
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ControlOutlined,
  ReloadOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined,
  ToolOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

// 数据状态
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref([])
const selectedRowKeys = ref([])

// 弹窗状态
const modalVisible = ref(false)
const controlModalVisible = ref(false)
const isEdit = ref(false)
const currentDevice = ref(null)
const formRef = ref()

// 统计数据
const stats = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  runningDevices: 0,
  faultDevices: 0
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

// 表单数据
const form = reactive({
  name: '',
  type: undefined,
  code: '',
  areaId: undefined,
  location: '',
  description: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请输入设备编号', trigger: 'blur' }
  ],
  areaId: [
    { required: true, message: '请选择所属分区', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入安装位置', trigger: 'blur' }
  ]
}

// 选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  }
}

// 是否有选择
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '设备名称',
    dataIndex: 'name',
    key: 'name',
    width: 150
  },
  {
    title: '设备类型',
    key: 'type',
    width: 120
  },
  {
    title: '设备编号',
    dataIndex: 'code',
    key: 'code',
    width: 120
  },
  {
    title: '所属分区',
    key: 'area',
    width: 100
  },
  {
    title: '状态',
    key: 'status',
    width: 120
  },
  {
    title: '运行参数',
    key: 'parameters',
    width: 200
  },
  {
    title: '定时配置',
    key: 'schedule',
    width: 120
  },
  {
    title: '安装位置',
    dataIndex: 'location',
    key: 'location',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 200
  }
]

// 获取在线状态颜色
const getOnlineColor = (online) => {
  return online ? 'green' : 'red'
}

// 获取设备类型颜色
const getTypeColor = (type) => {
  const colors = {
    fan: 'blue',
    light: 'yellow',
    pump: 'cyan',
    heater: 'red',
    cooler: 'green',
    sensor: 'purple',
    controller: 'orange'
  }
  return colors[type] || 'default'
}

// 获取设备类型文本
const getTypeText = (type) => {
  const texts = {
    fan: '通风设备',
    light: '照明设备',
    pump: '水泵设备',
    heater: '加热设备',
    cooler: '制冷设备',
    sensor: '传感器',
    controller: '控制器'
  }
  return texts[type] || type
}

// 获取参数提示
const getParametersTooltip = (parameters) => {
  return parameters.map(p => `${p.name}: ${p.value}${p.unit}`).join('\n')
}

// 生成模拟数据
const generateMockData = () => {
  const deviceTypes = ['fan', 'light', 'pump', 'heater', 'cooler', 'sensor', 'controller']
  const areas = ['A区', 'B区', 'C区']
  const locations = ['东区', '西区', '南区', '北区', '中央']

  return Array.from({ length: 50 }, (_, i) => {
    const type = deviceTypes[Math.floor(Math.random() * deviceTypes.length)]
    const online = Math.random() > 0.2
    const running = online && Math.random() > 0.3

    let parameters = []
    switch (type) {
      case 'fan':
        parameters = [
          { name: '转速', value: Math.floor(Math.random() * 100), unit: '%', min: 0, max: 100 },
          { name: '功率', value: Math.floor(Math.random() * 500), unit: 'W', min: 0, max: 1000 }
        ]
        break
      case 'light':
        parameters = [
          { name: '亮度', value: Math.floor(Math.random() * 100), unit: '%', min: 0, max: 100 },
          { name: '色温', value: Math.floor(Math.random() * 3000) + 3000, unit: 'K', min: 3000, max: 6000 }
        ]
        break
      case 'pump':
        parameters = [
          { name: '流量', value: Math.floor(Math.random() * 100), unit: 'L/h', min: 0, max: 200 },
          { name: '压力', value: (Math.random() * 2).toFixed(1), unit: 'bar', min: 0, max: 5 }
        ]
        break
      default:
        parameters = [
          { name: '功率', value: Math.floor(Math.random() * 100), unit: '%', min: 0, max: 100 }
        ]
    }

    return {
      id: i + 1,
      name: `设备${String(i + 1).padStart(3, '0')}`,
      type,
      code: `DEV${String(i + 1).padStart(4, '0')}`,
      areaId: Math.floor(Math.random() * 3) + 1,
      areaName: areas[Math.floor(Math.random() * areas.length)],
      location: locations[Math.floor(Math.random() * locations.length)],
      online,
      running,
      loading: false,
      hasSchedule: Math.random() > 0.7,
      parameters,
      description: `这是一个${getTypeText(type)}，用于${getDevicePurpose(type)}`
    }
  })
}

const getDevicePurpose = (type) => {
  const purposes = {
    fan: '空气循环和通风',
    light: '提供植物生长所需光照',
    pump: '灌溉和营养液循环',
    heater: '环境温度调节',
    cooler: '环境降温',
    sensor: '环境数据监测',
    controller: '自动化控制'
  }
  return purposes[type] || '未知用途'
}

// 加载设备数据
const loadDevices = () => {
  loading.value = true
  setTimeout(() => {
    dataSource.value = generateMockData()
    pagination.total = dataSource.value.length

    // 更新统计数据
    stats.totalDevices = dataSource.value.length
    stats.onlineDevices = dataSource.value.filter(d => d.online).length
    stats.runningDevices = dataSource.value.filter(d => d.running).length
    stats.faultDevices = dataSource.value.filter(d => !d.online).length

    loading.value = false
  }, 1000)
}

// 刷新数据
const handleRefresh = () => {
  selectedRowKeys.value = []
  loadDevices()
  message.success('数据刷新成功')
}

// 批量控制
const handleBatchControl = () => {
  message.info(`批量控制 ${selectedRowKeys.value.length} 个设备`)
}

// 控制设备
const controlDevice = (record) => {
  currentDevice.value = { ...record, parameters: [...record.parameters] }
  controlModalVisible.value = true
}

// 查看设备详情
const viewDetails = (record) => {
  message.info(`查看设备 ${record.name} 的详细信息`)
}

// 配置设备定时
const scheduleDevice = (record) => {
  message.info(`配置设备 ${record.name} 的定时任务`)
}

// 编辑设备
const editDevice = (record) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(form, record)
}

// 维护设备
const maintainDevice = (record) => {
  message.info(`设备 ${record.name} 进入维护模式`)
}

// 删除设备
const deleteDevice = (record) => {
  message.success(`设备 ${record.name} 删除成功`)
  loadDevices()
}

// 切换设备状态
const toggleDevice = (deviceId, checked) => {
  const device = dataSource.value.find(d => d.id === deviceId)
  if (device) {
    device.loading = true
    setTimeout(() => {
      device.running = checked
      device.loading = false
      message.success(`设备 ${device.name} 已${checked ? '开启' : '关闭'}`)
    }, 1000)
  }
}

// 更新设备参数
const updateDeviceParam = (deviceId, paramName, value) => {
  const device = dataSource.value.find(d => d.id === deviceId)
  if (device) {
    const param = device.parameters.find(p => p.name === paramName)
    if (param) {
      param.value = value
    }
  }
}

// 应用控制设置
const applyControlSettings = () => {
  message.success('设备控制设置已应用')
  controlModalVisible.value = false
  loadDevices()
}

// 重置控制设置
const resetControlSettings = () => {
  if (currentDevice.value) {
    controlDevice({ ...currentDevice.value })
  }
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    setTimeout(() => {
      if (isEdit.value) {
        const index = dataSource.value.findIndex(d => d.id === form.id)
        if (index > -1) {
          dataSource.value[index] = {
            ...dataSource.value[index],
            ...form,
            areaName: ['A区', 'B区', 'C区'][form.areaId - 1]
          }
        }
        message.success('设备更新成功')
      } else {
        const newDevice = {
          id: Date.now(),
          ...form,
          areaName: ['A区', 'B区', 'C区'][form.areaId - 1],
          online: true,
          running: false,
          loading: false,
          hasSchedule: false,
          parameters: [{ name: '功率', value: 50, unit: '%', min: 0, max: 100 }]
        }
        dataSource.value.push(newDevice)
        pagination.total++
        message.success('设备添加成功')
      }

      modalVisible.value = false
      submitLoading.value = false
      resetForm()
      loadDevices()
    }, 1000)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 取消弹窗
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    type: undefined,
    code: '',
    areaId: undefined,
    location: '',
    description: ''
  })
  formRef.value?.resetFields()
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadDevices()
}

// 组件挂载
onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.devices-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.selected-info {
  margin-top: 16px;
  padding: 8px 12px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  color: #1890ff;
  font-size: 14px;
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

.parameters-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.param-item {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.param-name {
  color: #8c8c8c;
}

.param-value {
  color: #262626;
  font-weight: 500;
}

.device-control {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-info h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.control-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.control-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-item label {
  font-weight: 500;
  color: #262626;
}

.param-display {
  text-align: right;
  font-size: 12px;
  color: #8c8c8c;
}

.quick-actions {
  text-align: right;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }

  .stats-row .ant-col {
    margin-bottom: 8px;
  }
}
</style>