<template>
  <div class="environment-monitoring">
    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="searchForm"
        layout="inline"
        @submit="handleSearch"
      >
        <a-form-item label="传感器ID">
          <a-select
            v-model:value="searchForm.sensorId"
            placeholder="请选择传感器"
            allow-clear
            style="width: 200px"
            show-search
            :filter-option="filterSensorOption"
          >
            <a-select-option
              v-for="sensorId in sensorIdList"
              :key="sensorId"
              :value="sensorId"
            >
              {{ sensorId }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="数据类型">
          <a-select
            v-model:value="searchForm.dataType"
            placeholder="请选择数据类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="(type, key) in DataType"
              :key="key"
              :value="type"
            >
              {{ getDataTypeDescription(type) }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="数据状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option
              v-for="(status, key) in DataStatus"
              :key="key"
              :value="status"
            >
              {{ getDataStatusDescription(status) }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="searchForm.timeRange"
            show-time
            format="YYYY-MM-DD HH:mm"
            style="width: 350px"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="searchLoading">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
            <a-button @click="createMockData" :loading="mockLoading">
              <template #icon>
                <ExperimentOutlined />
              </template>
              创建模拟数据
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮 -->
    <a-card class="action-card" :bordered="false">
      <a-space>
        <a-button type="primary" @click="showAddModal">
          <template #icon>
            <PlusOutlined />
          </template>
          记录数据
        </a-button>
        <a-button @click="refreshData">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </a-space>
    </a-card>

    <!-- 数据表格 -->
    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :row-key="record => record.id"
        @change="handleTableChange"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'value'">
            <span :class="getValueClass(record)">
              {{ record.value }} {{ record.unit || getDataTypeUnit(record.dataType) }}
            </span>
          </template>
          <template v-if="column.key === 'dataType'">
            <a-tag :color="getDataTypeColor(record.dataType)">
              {{ getDataTypeDescription(record.dataType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getDataStatusDescription(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'threshold'">
            <span v-if="record.minThreshold || record.maxThreshold">
              {{ record.minThreshold }} - {{ record.maxThreshold }}
            </span>
            <span v-else class="text-gray-400">未设置</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑阈值
              </a-button>
              <a-popconfirm
                title="确定删除这条记录吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加数据模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="传感器ID" name="sensorId">
              <a-input
                v-model:value="form.sensorId"
                placeholder="请输入传感器ID"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据类型" name="dataType">
              <a-select
                v-model:value="form.dataType"
                placeholder="请选择数据类型"
              >
                <a-select-option
                  v-for="(type, key) in DataType"
                  :key="key"
                  :value="type"
                >
                  {{ getDataTypeDescription(type) }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数值" name="value">
              <a-input-number
                v-model:value="form.value"
                :precision="2"
                :min="-999.99"
                :max="999.99"
                style="width: 100%"
                placeholder="请输入数值"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据时间" name="collectedAt">
              <a-date-picker
                v-model:value="form.collectedAt"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最小阈值" name="minThreshold">
              <a-input-number
                v-model:value="form.minThreshold"
                :precision="2"
                style="width: 100%"
                placeholder="可选"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大阈值" name="maxThreshold">
              <a-input-number
                v-model:value="form.maxThreshold"
                :precision="2"
                style="width: 100%"
                placeholder="可选"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注" name="remarks">
          <a-textarea
            v-model:value="form.remarks"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑阈值模态框 -->
    <a-modal
      v-model:open="thresholdModalVisible"
      title="编辑阈值设置"
      width="400px"
      @ok="handleThresholdOk"
      @cancel="handleThresholdCancel"
    >
      <a-form
        ref="thresholdFormRef"
        :model="thresholdForm"
        layout="vertical"
      >
        <a-form-item label="最小阈值">
          <a-input-number
            v-model:value="thresholdForm.minThreshold"
            :precision="2"
            style="width: 100%"
            placeholder="请输入最小阈值"
          />
        </a-form-item>
        <a-form-item label="最大阈值">
          <a-input-number
            v-model:value="thresholdForm.maxThreshold"
            :precision="2"
            style="width: 100%"
            placeholder="请输入最大阈值"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ExperimentOutlined
} from '@ant-design/icons-vue'
import {
  getDataByTimeRange,
  saveEnvironmentData,
  deleteEnvironmentData,
  updateThresholds,
  getAllActiveSensorIds,
  createMockData as createMockDataApi,
  DataType,
  DataStatus
} from '@/api/environment-data'

// 响应式数据
const searchForm = reactive({
  sensorId: undefined,
  dataType: undefined,
  status: undefined,
  timeRange: []
})

const form = reactive({
  sensorId: '',
  dataType: undefined,
  value: null,
  collectedAt: null,
  minThreshold: null,
  maxThreshold: null,
  remarks: ''
})

const thresholdForm = reactive({
  minThreshold: null,
  maxThreshold: null
})

const tableData = ref([])
const sensorIdList = ref([])
const tableLoading = ref(false)
const searchLoading = ref(false)
const mockLoading = ref(false)
const modalVisible = ref(false)
const thresholdModalVisible = ref(false)
const currentEditId = ref(null)

const formRef = ref()
const thresholdFormRef = ref()

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 表格列配置
const tableColumns = [
  {
    title: '传感器ID',
    dataIndex: 'sensorId',
    key: 'sensorId',
    width: 120,
    fixed: 'left'
  },
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'dataType',
    width: 120
  },
  {
    title: '数值',
    dataIndex: 'value',
    key: 'value',
    width: 120,
    sorter: true
  },
  {
    title: '单位',
    dataIndex: 'unit',
    key: 'unit',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '阈值范围',
    key: 'threshold',
    width: 120
  },
  {
    title: '采集时间',
    dataIndex: 'collectedAt',
    key: 'collectedAt',
    width: 180,
    sorter: true
  },
  {
    title: '数据来源',
    dataIndex: 'source',
    key: 'source',
    width: 100
  },
  {
    title: '备注',
    dataIndex: 'remarks',
    key: 'remarks',
    width: 150,
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 表单验证规则
const formRules = {
  sensorId: [
    { required: true, message: '请输入传感器ID!' }
  ],
  dataType: [
    { required: true, message: '请选择数据类型!' }
  ],
  value: [
    { required: true, message: '请输入数值!' }
  ],
  collectedAt: [
    { required: true, message: '请选择数据时间!' }
  ]
}

// 计算属性
const modalTitle = computed(() => '记录环境数据')

// 方法
const getDataTypeDescription = (type) => {
  const descriptions = {
    [DataType.TEMPERATURE]: '温度',
    [DataType.HUMIDITY]: '湿度',
    [DataType.LIGHT_INTENSITY]: '光照强度',
    [DataType.CO2]: 'CO2',
    [DataType.SOIL_MOISTURE]: '土壤湿度',
    [DataType.PH]: 'pH值'
  }
  return descriptions[type] || type
}

const getDataTypeUnit = (type) => {
  const units = {
    [DataType.TEMPERATURE]: '°C',
    [DataType.HUMIDITY]: '%RH',
    [DataType.LIGHT_INTENSITY]: 'lx',
    [DataType.CO2]: 'ppm',
    [DataType.SOIL_MOISTURE]: '%',
    [DataType.PH]: 'pH'
  }
  return units[type] || ''
}

const getDataStatusDescription = (status) => {
  const descriptions = {
    [DataStatus.NORMAL]: '正常',
    [DataStatus.ABNORMAL]: '异常',
    [DataStatus.WARNING]: '警告'
  }
  return descriptions[status] || status
}

const getDataTypeColor = (type) => {
  const colors = {
    [DataType.TEMPERATURE]: 'red',
    [DataType.HUMIDITY]: 'blue',
    [DataType.LIGHT_INTENSITY]: 'orange',
    [DataType.CO2]: 'purple',
    [DataType.SOIL_MOISTURE]: 'green',
    [DataType.PH]: 'cyan'
  }
  return colors[type] || 'default'
}

const getStatusColor = (status) => {
  const colors = {
    [DataStatus.NORMAL]: 'green',
    [DataStatus.ABNORMAL]: 'red',
    [DataStatus.WARNING]: 'orange'
  }
  return colors[status] || 'default'
}

const getValueClass = (record) => {
  if (record.status === DataStatus.WARNING) {
    return 'warning-value'
  }
  if (record.status === DataStatus.ABNORMAL) {
    return 'abnormal-value'
  }
  return 'normal-value'
}

const filterSensorOption = (input, option) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const fetchSensorIds = async () => {
  try {
    const response = await getAllActiveSensorIds()
    sensorIdList.value = response.data || []
  } catch (error) {
    console.error('获取传感器ID列表失败:', error)
  }
}

const fetchTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      sortBy: 'collectedAt',
      sortDir: 'desc'
    }

    // 添加搜索条件
    if (searchForm.sensorId) {
      params.sensorId = searchForm.sensorId
    }
    if (searchForm.dataType) {
      params.dataType = searchForm.dataType
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0].format('YYYY-MM-DDTHH:mm:ss')
      params.endTime = searchForm.timeRange[1].format('YYYY-MM-DDTHH:mm:ss')
    }

    const response = await getDataByTimeRange(params)
    tableData.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
  } catch (error) {
    console.error('获取环境数据失败:', error)
    message.error('获取环境数据失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    sensorId: undefined,
    dataType: undefined,
    status: undefined,
    timeRange: []
  })
  pagination.current = 1
  fetchTableData()
}

const refreshData = () => {
  fetchTableData()
}

const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const showAddModal = () => {
  currentEditId.value = null
  resetForm()
  form.collectedAt = dayjs()
  modalVisible.value = true
}

const handleView = (record) => {
  // 查看详情功能
  message.info('查看详情功能开发中...')
}

const handleEdit = (record) => {
  currentEditId.value = record.id
  thresholdForm.minThreshold = record.minThreshold
  thresholdForm.maxThreshold = record.maxThreshold
  thresholdModalVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    sensorId: '',
    dataType: undefined,
    value: null,
    collectedAt: null,
    minThreshold: null,
    maxThreshold: null,
    remarks: ''
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()

    const submitData = {
      ...form,
      collectedAt: form.collectedAt.format('YYYY-MM-DDTHH:mm:ss')
    }

    await saveEnvironmentData(submitData)
    message.success('数据记录成功')
    modalVisible.value = false
    fetchTableData()
  } catch (error) {
    console.error('记录数据失败:', error)
    if (error.errorFields) {
      message.error('请检查表单数据')
    } else {
      message.error('记录数据失败')
    }
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const handleThresholdOk = async () => {
  try {
    await updateThresholds(currentEditId.value, thresholdForm)
    message.success('阈值更新成功')
    thresholdModalVisible.value = false
    fetchTableData()
  } catch (error) {
    console.error('更新阈值失败:', error)
    message.error('更新阈值失败')
  }
}

const handleThresholdCancel = () => {
  thresholdModalVisible.value = false
  Object.assign(thresholdForm, {
    minThreshold: null,
    maxThreshold: null
  })
}

const handleDelete = async (id) => {
  try {
    await deleteEnvironmentData(id)
    message.success('删除成功')
    fetchTableData()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const createMockData = async () => {
  mockLoading.value = true
  try {
    await createMockDataApi()
    message.success('模拟数据创建成功')
    fetchTableData()
    fetchSensorIds() // 刷新传感器列表
  } catch (error) {
    console.error('创建模拟数据失败:', error)
    message.error('创建模拟数据失败')
  } finally {
    mockLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchSensorIds()
  fetchTableData()
})
</script>

<style scoped lang="less">
.environment-monitoring {
  .search-card,
  .action-card,
  .table-card {
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }

  .normal-value {
    color: #52c41a;
    font-weight: 600;
  }

  .warning-value {
    color: #faad14;
    font-weight: 600;
  }

  .abnormal-value {
    color: #ff4d4f;
    font-weight: 600;
  }

  .text-gray-400 {
    color: #bfbfbf;
  }
}

@media (max-width: 768px) {
  .environment-monitoring {
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }

    :deep(.ant-table) {
      font-size: 12px;
    }
  }
}
</style>