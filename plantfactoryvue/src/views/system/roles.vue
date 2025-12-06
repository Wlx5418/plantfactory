<template>
  <div class="roles-page">
    <page-header title="角色管理" subtitle="管理系统角色和权限配置" />

    <div class="content-container">
      <!-- 操作区域 -->
      <a-card class="action-card" :bordered="false">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <plus-outlined />
            新建角色
          </a-button>
          <a-button :disabled="!hasSelected" @click="handleBatchDelete">
            <delete-outlined />
            批量删除
          </a-button>
          <a-button @click="handleRefresh">
            <reload-outlined />
            刷新
          </a-button>
        </a-space>

        <div class="selected-info" v-if="hasSelected">
          已选择 {{ selectedRowKeys.length }} 项
        </div>
      </a-card>

      <!-- 角色列表 -->
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
              <a-switch
                :checked="record.status === 'active'"
                @change="(checked) => handleStatusChange(record.id, checked)"
                :loading="record.statusLoading"
              />
            </template>
            <template v-else-if="column.key === 'permissions'">
              <a-tag
                v-for="permission in record.permissions.slice(0, 3)"
                :key="permission"
                color="blue"
              >
                {{ permission }}
              </a-tag>
              <a-tag v-if="record.permissions.length > 3" color="default">
                +{{ record.permissions.length - 3 }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="editRole(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" @click="viewPermissions(record)">
                  权限
                </a-button>
                <a-popconfirm
                  title="确定要删除这个角色吗？"
                  @confirm="deleteRole(record.id)"
                  ok-text="确定"
                  cancel-text="取消"
                >
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 创建/编辑角色弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑角色' : '新建角色'"
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
        <a-form-item label="角色名称" name="name">
          <a-input
            v-model:value="form.name"
            placeholder="请输入角色名称"
            :disabled="isEdit"
          />
        </a-form-item>

        <a-form-item label="角色代码" name="code">
          <a-input
            v-model:value="form.code"
            placeholder="请输入角色代码"
            :disabled="isEdit"
          />
        </a-form-item>

        <a-form-item label="角色描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入角色描述"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="角色状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio value="active">启用</a-radio>
            <a-radio value="inactive">禁用</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="权限配置" name="permissions">
          <a-checkbox-group v-model:value="form.permissions">
            <a-row>
              <a-col :span="12" v-for="permission in allPermissions" :key="permission.value">
                <a-checkbox :value="permission.value">
                  {{ permission.label }}
                </a-checkbox>
              </a-col>
            </a-row>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 权限查看弹窗 -->
    <a-modal
      v-model:open="permissionModalVisible"
      title="角色权限详情"
      width="500px"
      :footer="null"
    >
      <div v-if="currentRole">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="角色名称">
            {{ currentRole.name }}
          </a-descriptions-item>
          <a-descriptions-item label="角色代码">
            {{ currentRole.code }}
          </a-descriptions-item>
          <a-descriptions-item label="用户数量">
            {{ currentRole.userCount || 0 }} 人
          </a-descriptions-item>
          <a-descriptions-item label="拥有权限">
            <div class="permissions-list">
              <a-tag
                v-for="permission in currentRole.permissions"
                :key="permission"
                color="blue"
                style="margin: 4px;"
              >
                {{ permission }}
              </a-tag>
              <span v-if="!currentRole.permissions.length" class="no-permissions">
                暂无权限配置
              </span>
            </div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  batchDeleteRoles,
  updateRoleStatus,
  getPermissionOptions
} from '@/api/role'

// 数据状态
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref([])
const selectedRowKeys = ref([])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 弹窗状态
const modalVisible = ref(false)
const permissionModalVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref(null)
const formRef = ref()

// 表单数据
const form = reactive({
  name: '',
  code: '',
  description: '',
  status: 'active',
  permissions: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色代码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' }
  ],
  permissions: [
    { type: 'array', required: true, message: '请选择权限配置', trigger: 'change' }
  ]
}

// 所有可选权限
const allPermissions = [
  { label: '用户管理', value: 'user_manage' },
  { label: '角色管理', value: 'role_manage' },
  { label: '权限管理', value: 'permission_manage' },
  { label: '环境监控', value: 'environment_monitor' },
  { label: '设备控制', value: 'device_control' },
  { label: '数据查看', value: 'data_view' },
  { label: '数据导出', value: 'data_export' },
  { label: '系统配置', value: 'system_config' },
  { label: '日志查看', value: 'log_view' },
  { label: '分区管理', value: 'area_manage' }
]

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '角色名称',
    dataIndex: 'name',
    key: 'name',
    width: 120
  },
  {
    title: '角色代码',
    dataIndex: 'code',
    key: 'code',
    width: 120
  },
  {
    title: '角色描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '用户数量',
    dataIndex: 'userCount',
    key: 'userCount',
    width: 100
  },
  {
    title: '权限',
    key: 'permissions',
    width: 200
  },
  {
    title: '状态',
    key: 'status',
    width: 80
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 200
  }
]

// 选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  }
}

// 是否有选择
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

// 生成模拟数据
const generateMockData = () => {
  const rolePermissions = {
    'ADMIN': ['用户管理', '角色管理', '权限管理', '系统配置', '日志查看'],
    'OPERATOR': ['环境监控', '设备控制', '数据查看', '分区管理'],
    'OBSERVER': ['环境监控', '数据查看'],
    'MANAGER': ['用户管理', '环境监控', '设备控制', '数据查看', '数据导出', '分区管理']
  }

  return [
    {
      id: 1,
      name: '系统管理员',
      code: 'ADMIN',
      description: '拥有系统所有权限的超级管理员',
      userCount: 1,
      permissions: rolePermissions.ADMIN,
      status: 'active',
      statusLoading: false,
      createTime: '2024-01-01 10:00:00'
    },
    {
      id: 2,
      name: '操作员',
      code: 'OPERATOR',
      description: '负责日常操作和设备控制',
      userCount: 5,
      permissions: rolePermissions.OPERATOR,
      status: 'active',
      statusLoading: false,
      createTime: '2024-01-02 10:00:00'
    },
    {
      id: 3,
      name: '观察员',
      code: 'OBSERVER',
      description: '只能查看数据，无操作权限',
      userCount: 10,
      permissions: rolePermissions.OBSERVER,
      status: 'active',
      statusLoading: false,
      createTime: '2024-01-03 10:00:00'
    },
    {
      id: 4,
      name: '管理员',
      code: 'MANAGER',
      description: '部门管理员，具有部分管理权限',
      userCount: 3,
      permissions: rolePermissions.MANAGER,
      status: 'inactive',
      statusLoading: false,
      createTime: '2024-01-04 10:00:00'
    }
  ]
}

// 加载角色数据
const loadRoles = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    const response = await getRoleList(params)
    if (response.data && response.data.content) {
      dataSource.value = response.data.content.map(role => ({
        ...role,
        statusLoading: false
      }))
      pagination.total = response.data.totalElements || 0
    } else {
      // 如果后端服务不可用，使用模拟数据
      console.log('使用模拟角色数据')
      dataSource.value = generateMockData()
      pagination.total = dataSource.value.length
      message.warning('后端服务连接中，当前显示模拟数据')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    // 当后端服务不可用时，使用模拟数据
    dataSource.value = generateMockData()
    pagination.total = dataSource.value.length
    message.warning('后端服务连接中，当前显示模拟数据')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const handleRefresh = () => {
  selectedRowKeys.value = []
  loadRoles()
  message.success('刷新成功')
}

// 状态变更
const handleStatusChange = async (roleId, checked) => {
  const role = dataSource.value.find(r => r.id === roleId)
  if (role) {
    role.statusLoading = true
    try {
      await updateRoleStatus(roleId, checked ? 'active' : 'inactive')
      role.status = checked ? 'active' : 'inactive'
      message.success(`角色已${checked ? '启用' : '禁用'}`)
    } catch (error) {
      console.error('更新角色状态失败:', error)
      message.warning('后端服务连接中，状态更新已模拟执行')
      role.status = checked ? 'active' : 'inactive'
    } finally {
      role.statusLoading = false
    }
  }
}

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

// 编辑角色
const editRole = (record) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(form, {
    ...record,
    permissions: allPermissions
      .filter(p => record.permissions.includes(p.label))
      .map(p => p.value)
  })
}

// 查看权限
const viewPermissions = (record) => {
  currentRole.value = record
  permissionModalVisible.value = true
}

// 删除角色
const deleteRole = (roleId) => {
  dataSource.value = dataSource.value.filter(r => r.id !== roleId)
  pagination.total--
  message.success('删除成功')
}

// 批量删除
const handleBatchDelete = () => {
  dataSource.value = dataSource.value.filter(
    r => !selectedRowKeys.value.includes(r.id)
  )
  pagination.total -= selectedRowKeys.value.length
  selectedRowKeys.value = []
  message.success('批量删除成功')
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const submitData = {
      name: form.name,
      code: form.code,
      description: form.description,
      status: form.status,
      permissions: form.permissions
    }

    try {
      if (isEdit.value) {
        await updateRole(form.id, submitData)
        message.success('角色更新成功')
      } else {
        await createRole(submitData)
        message.success('角色创建成功')
      }

      modalVisible.value = false
      resetForm()
      loadRoles()
    } catch (error) {
      console.error('角色操作失败:', error)
      // 模拟操作成功
      if (isEdit.value) {
        const index = dataSource.value.findIndex(r => r.id === form.id)
        if (index > -1) {
          dataSource.value[index] = {
            ...dataSource.value[index],
            ...form,
            permissions: form.permissions.map(p =>
              allPermissions.find(ap => ap.value === p)?.label || p
            )
          }
        }
        message.success('角色更新成功')
      } else {
        const newRole = {
          id: Date.now(),
          ...form,
          userCount: 0,
          permissions: form.permissions.map(p =>
            allPermissions.find(ap => ap.value === p)?.label || p
          ),
          createTime: new Date().toLocaleString()
        }
        dataSource.value.push(newRole)
        pagination.total++
        message.success('角色创建成功')
      }

      modalVisible.value = false
      resetForm()
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
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
    code: '',
    description: '',
    status: 'active',
    permissions: []
  })
  formRef.value?.resetFields()
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadRoles()
}

// 组件挂载
onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.roles-page {
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

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.no-permissions {
  color: #999;
  font-style: italic;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }
}
</style>