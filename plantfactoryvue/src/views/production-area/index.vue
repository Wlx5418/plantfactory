<template>
  <div class="production-area">
    <a-card class="table-card" :bordered="false">
      <template #title>
        <span>生产区域管理</span>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon>
              <PlusOutlined />
            </template>
            新增区域
          </a-button>
          <a-button @click="refreshData">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </template>

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
          <template v-if="column.key === 'usageRate'">
            <a-progress
              :percent="parseFloat(record.usageRate || 0)"
              :status="record.usageRate > 80 ? 'exception' : 'normal'"
              size="small"
            />
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusDesc }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="viewUsage(record)">
                使用情况
              </a-button>
              <a-popconfirm
                title="确定删除该区域吗？"
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
        <a-form-item label="区域名称" name="areaName">
          <a-input
            v-model:value="form.areaName"
            placeholder="请输入区域名称"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="区域类型" name="areaType">
              <a-select
                v-model:value="form.areaType"
                placeholder="请选择区域类型"
              >
                <a-select-option value="GROWING">生长区</a-select-option>
                <a-select-option value="SEEDLING">育苗区</a-select-option>
                <a-select-option value="HARVEST">采收区</a-select-option>
                <a-select-option value="STORAGE">存储区</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="容量" name="capacity">
              <a-input-number
                v-model:value="form.capacity"
                :min="0"
                style="width: 100%"
                placeholder="请输入容量"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="当前使用量" name="currentUsage">
              <a-input-number
                v-model:value="form.currentUsage"
                :min="0"
                style="width: 100%"
                placeholder="请输入当前使用量"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-select
                v-model:value="form.status"
                placeholder="请选择状态"
              >
                <a-select-option value="ACTIVE">正常</a-select-option>
                <a-select-option value="MAINTENANCE">维护中</a-select-option>
                <a-select-option value="INACTIVE">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="form.description"
            :rows="4"
            placeholder="请输入区域描述"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  getProductionAreaList,
  createProductionArea,
  updateProductionArea,
  deleteProductionArea
} from '@/api/production-area'

// 响应式数据
const form = reactive({
  id: null,
  areaName: '',
  areaType: '',
  capacity: null,
  currentUsage: null,
  status: 'ACTIVE',
  description: ''
})

const tableData = ref([])
const tableLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)

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
    width: 150
  },
  {
    title: '区域类型',
    dataIndex: 'areaTypeDesc',
    key: 'areaTypeDesc',
    width: 120
  },
  {
    title: '容量',
    dataIndex: 'capacity',
    key: 'capacity',
    width: 100,
    render: (value) => `${value} 平方米`
  },
  {
    title: '当前使用量',
    dataIndex: 'currentUsage',
    key: 'currentUsage',
    width: 120,
    render: (value) => `${value} 平方米`
  },
  {
    title: '使用率',
    dataIndex: 'usageRate',
    key: 'usageRate',
    width: 150
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    width: 200,
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180
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
  areaName: [
    { required: true, message: '请输入区域名称!' },
    { max: 100, message: '区域名称不能超过 100 个字符' }
  ],
  areaType: [
    { required: true, message: '请选择区域类型!' }
  ],
  capacity: [
    { required: true, type: 'number', min: 0, message: '请输入有效的容量!' }
  ],
  currentUsage: [
    { type: 'number', min: 0, message: '使用量不能为负数!' }
  ]
}

// 计算属性
const modalTitle = computed(() => isEdit.value ? '编辑生产区域' : '新增生产区域')

// 方法
const fetchTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }

    const response = await getProductionAreaList(params)

    // 模拟使用率计算
    const data = response.data || []
    tableData.value = data.map(item => ({
      ...item,
      usageRate: item.capacity > 0 ?
        ((item.currentUsage / item.capacity) * 100).toFixed(1) : 0,
      areaTypeDesc: getAreaTypeDesc(item.areaType),
      statusDesc: getStatusDesc(item.status)
    }))

    pagination.total = response.pagination?.total || 0
  } catch (error) {
    console.error('获取生产区域列表失败:', error)
    message.error('获取生产区域列表失败')
  } finally {
    tableLoading.value = false
  }
}

const getAreaTypeDesc = (type) => {
  const types = {
    'GROWING': '生长区',
    'SEEDLING': '育苗区',
    'HARVEST': '采收区',
    'STORAGE': '存储区'
  }
  return types[type] || type
}

const getStatusDesc = (status) => {
  const statuses = {
    'ACTIVE': '正常',
    'MAINTENANCE': '维护中',
    'INACTIVE': '停用'
  }
  return statuses[status] || status
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
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(form, { ...record })
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    areaName: '',
    areaType: '',
    capacity: null,
    currentUsage: null,
    status: 'ACTIVE',
    description: ''
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()

    const submitData = { ...form }
    if (!isEdit.value) {
      delete submitData.id
    }

    if (isEdit.value) {
      await updateProductionArea(form.id, submitData)
      message.success('更新成功')
    } else {
      await createProductionArea(submitData)
      message.success('创建成功')
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
    await deleteProductionArea(id)
    message.success('删除成功')
    fetchTableData()
  } catch (error) {
    console.error('删除生产区域失败:', error)
    message.error('删除失败')
  }
}

const viewUsage = (record) => {
  message.info(`查看区域 "${record.areaName}" 的使用情况`)
}

const getStatusColor = (status) => {
  const colors = {
    'ACTIVE': 'green',
    'MAINTENANCE': 'orange',
    'INACTIVE': 'red'
  }
  return colors[status] || 'default'
}

// 生命周期
onMounted(() => {
  fetchTableData()
})
</script>

<style scoped lang="less">
.production-area {
  .table-card {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }
}

@media (max-width: 768px) {
  .production-area {
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }

    :deep(.ant-table) {
      font-size: 12px;
    }
  }
}
</style>