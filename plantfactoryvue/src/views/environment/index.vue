<template>
  <div class="environment-monitoring">
    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="searchForm"
        layout="inline"
        @submit="handleSearch"
      >
        <a-form-item label="生产区域">
          <a-select
            v-model:value="searchForm.areaId"
            placeholder="请选择区域"
            allow-clear
            style="width: 200px"
          >
            <a-select-option
              v-for="area in areaList"
              :key="area.id"
              :value="area.id"
            >
              {{ area.areaName }}
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

        <a-form-item label="数据状态">
          <a-select
            v-model:value="searchForm.qualityFlag"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="GOOD">良好</a-select-option>
            <a-select-option value="WARNING">警告</a-select-option>
            <a-select-option value="ERROR">错误</a-select-option>
          </a-select>
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
        <a-button @click="handleExport">
          <template #icon>
            <ExportOutlined />
          </template>
          导出数据
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
          <template v-if="column.key === 'temperature'">
            <span :class="getValueClass(record.temperature, 15, 30)">
              {{ record.temperature }}°C
            </span>
          </template>
          <template v-if="column.key === 'humidity'">
            <span :class="getValueClass(record.humidity, 40, 80)">
              {{ record.humidity }}%
            </span>
          </template>
          <template v-if="column.key === 'lightIntensity'">
            <span :class="getValueClass(record.lightIntensity, 2000, 8000)">
              {{ record.lightIntensity }} lux
            </span>
          </template>
          <template v-if="column.key === 'qualityFlag'">
            <a-tag :color="getQualityColor(record.qualityFlag)">
              {{ record.qualityFlagDesc }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
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

    <!-- 添加/编辑模态框 -->
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
            <a-form-item label="生产区域" name="areaId">
              <a-select
                v-model:value="form.areaId"
                placeholder="请选择区域"
              >
                <a-select-option
                  v-for="area in areaList"
                  :key="area.id"
                  :value="area.id"
                >
                  {{ area.areaName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据时间" name="dataTime">
              <a-date-picker
                v-model:value="form.dataTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="温度 (°C)" name="temperature">
              <a-input-number
                v-model:value="form.temperature"
                :min="-50"
                :max="100"
                :precision="1"
                style="width: 100%"
                placeholder="请输入温度"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="湿度 (%)" name="humidity">
              <a-input-number
                v-model:value="form.humidity"
                :min="0"
                :max="100"
                :precision="1"
                style="width: 100%"
                placeholder="请输入湿度"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="光照强度 (lux)" name="lightIntensity">
              <a-input-number
                v-model:value="form.lightIntensity"
                :min="0"
                style="width: 100%"
                placeholder="请输入光照强度"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="CO2浓度 (ppm)" name="co2Level">
              <a-input-number
                v-model:value="form.co2Level"
                :min="0"
                style="width: 100%"
                placeholder="请输入CO2浓度"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="pH值" name="phValue">
              <a-input-number
                v-model:value="form.phValue"
                :min="0"
                :max="14"
                :precision="2"
                style="width: 100%"
                placeholder="请输入pH值"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="EC值 (mS/cm)" name="ecValue">
              <a-input-number
                v-model:value="form.ecValue"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入EC值"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="土壤湿度 (%)" name="soilMoisture">
              <a-input-number
                v-model:value="form.soilMoisture"
                :min="0"
                :max="100"
                :precision="1"
                style="width: 100%"
                placeholder="请输入土壤湿度"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据质量" name="qualityFlag">
              <a-select
                v-model:value="form.qualityFlag"
                placeholder="请选择数据质量"
              >
                <a-select-option value="GOOD">良好</a-select-option>
                <a-select-option value="WARNING">警告</a-select-option>
                <a-select-option value="ERROR">错误</a-select-option>
              </a-select>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ExportOutlined
} from '@ant-design/icons-vue'
import {
  getEnvironmentDataList,
  createEnvironmentData,
  updateEnvironmentData,
  deleteEnvironmentData,
  getProductionAreaList
} from '@/api/environment-data'

// 响应式数据
const searchForm = reactive({
  areaId: undefined,
  timeRange: [],
  qualityFlag: undefined
})

const form = reactive({
  areaId: undefined,
  dataTime: null,
  temperature: null,
  humidity: null,
  lightIntensity: null,
  co2Level: null,
  phValue: null,
  ecValue: null,
  soilMoisture: null,
  qualityFlag: 'GOOD',
  remarks: ''
})

const tableData = ref([])
const areaList = ref([])
const tableLoading = ref(false)
const searchLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

const formRef = ref()

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 表格列配置
const tableColumns = [
  {
    title: '区域名称',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 120
  },
  {
    title: '数据时间',
    dataIndex: 'dataTime',
    key: 'dataTime',
    width: 180
  },
  {
    title: '温度',
    dataIndex: 'temperature',
    key: 'temperature',
    width: 100,
    sorter: true
  },
  {
    title: '湿度',
    dataIndex: 'humidity',
    key: 'humidity',
    width: 100,
    sorter: true
  },
  {
    title: '光照强度',
    dataIndex: 'lightIntensity',
    key: 'lightIntensity',
    width: 120,
    sorter: true
  },
  {
    title: 'CO2浓度',
    dataIndex: 'co2Level',
    key: 'co2Level',
    width: 100,
    render: (value) => `${value} ppm`
  },
  {
    title: 'pH值',
    dataIndex: 'phValue',
    key: 'phValue',
    width: 80
  },
  {
    title: 'EC值',
    dataIndex: 'ecValue',
    key: 'ecValue',
    width: 100,
    render: (value) => `${value} mS/cm`
  },
  {
    title: '土壤湿度',
    dataIndex: 'soilMoisture',
    key: 'soilMoisture',
    width: 100,
    render: (value) => `${value}%`
  },
  {
    title: '数据质量',
    dataIndex: 'qualityFlag',
    key: 'qualityFlag',
    width: 100
  },
  {
    title: '操作人',
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
]

// 表单验证规则
const formRules = {
  areaId: [
    { required: true, message: '请选择生产区域!' }
  ],
  dataTime: [
    { required: true, message: '请选择数据时间!' }
  ],
  temperature: [
    { required: true, message: '请输入温度!' },
    { type: 'number', min: -50, max: 100, message: '温度范围为-50到100°C!' }
  ],
  humidity: [
    { type: 'number', min: 0, max: 100, message: '湿度范围为0到100%!' }
  ],
  lightIntensity: [
    { type: 'number', min: 0, message: '光照强度不能为负数!' }
  ],
  co2Level: [
    { type: 'number', min: 0, message: 'CO2浓度不能为负数!' }
  ],
  phValue: [
    { type: 'number', min: 0, max: 14, message: 'pH值范围为0到14!' }
  ],
  ecValue: [
    { type: 'number', min: 0, message: 'EC值不能为负数!' }
  ],
  soilMoisture: [
    { type: 'number', min: 0, max: 100, message: '土壤湿度范围为0到100%!' }
  ]
}

// 计算属性
const modalTitle = computed(() => isEdit.value ? '编辑环境数据' : '记录环境数据')

// 方法
const fetchAreaList = async () => {
  try {
    const response = await getProductionAreaList({ page: 0, size: 1000 })
    areaList.value = response.data || []
  } catch (error) {
    console.error('获取区域列表失败:', error)
  }
}

const fetchTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }

    // 添加搜索条件
    if (searchForm.areaId) {
      params.areaId = searchForm.areaId
    }
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0].format('YYYY-MM-DD HH:mm:ss')
      params.endTime = searchForm.timeRange[1].format('YYYY-MM-DD HH:mm:ss')
    }
    if (searchForm.qualityFlag) {
      params.qualityFlag = searchForm.qualityFlag
    }

    const response = await getEnvironmentDataList(params)
    tableData.value = response.data || []
    pagination.total = response.pagination?.total || 0
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
    areaId: undefined,
    timeRange: [],
    qualityFlag: undefined
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
  isEdit.value = false
  currentId.value = null
  resetForm()
  form.dataTime = dayjs()
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  currentId.value = record.id
  Object.assign(form, {
    ...record,
    dataTime: dayjs(record.dataTime)
  })
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    areaId: undefined,
    dataTime: null,
    temperature: null,
    humidity: null,
    lightIntensity: null,
    co2Level: null,
    phValue: null,
    ecValue: null,
    soilMoisture: null,
    qualityFlag: 'GOOD',
    remarks: ''
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()

    const submitData = {
      ...form,
      dataTime: form.dataTime.format('YYYY-MM-DD HH:mm:ss')
    }

    if (isEdit.value) {
      await updateEnvironmentData(currentId.value, submitData)
      message.success('更新成功')
    } else {
      await createEnvironmentData(submitData)
      message.success('添加成功')
    }

    modalVisible.value = false
    fetchTableData()
  } catch (error) {
    console.error('操作失败:', error)
    if (error.errorFields) {
      message.error('请检查表单数据')
    } else {
      message.error('操作失败')
    }
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
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

const handleExport = () => {
  message.info('导出功能开发中...')
}

const getValueClass = (value, min, max) => {
  if (value < min || value > max) {
    return 'abnormal-value'
  }
  return 'normal-value'
}

const getQualityColor = (quality) => {
  const colors = {
    'GOOD': 'green',
    'WARNING': 'orange',
    'ERROR': 'red'
  }
  return colors[quality] || 'default'
}

// 生命周期
onMounted(() => {
  fetchAreaList()
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

  .abnormal-value {
    color: #ff4d4f;
    font-weight: 600;
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