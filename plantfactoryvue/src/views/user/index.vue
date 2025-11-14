<template>
  <div class="user-management">
    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="searchForm"
        layout="inline"
        @submit="handleSearch"
      >
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.username"
            placeholder="请输入用户名"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="真实姓名">
          <a-input
            v-model:value="searchForm.realName"
            placeholder="请输入真实姓名"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="INACTIVE">未激活</a-select-option>
            <a-select-option value="LOCKED">已锁定</a-select-option>
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
          新增用户
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
          <template v-if="column.key === 'avatar'">
            <a-avatar :src="record.avatarUrl">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusDesc }}
            </a-tag>
          </template>
          <template v-if="column.key === 'lastLoginTime'">
            {{ record.lastLoginTime || '从未登录' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleResetPassword(record)">
                重置密码
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多
                  <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item
                      v-if="record.status === 'ACTIVE'"
                      @click="handleUpdateStatus(record.id, 'LOCKED')"
                    >
                      <LockOutlined />
                      锁定用户
                    </a-menu-item>
                    <a-menu-item
                      v-if="record.status === 'LOCKED'"
                      @click="handleUpdateStatus(record.id, 'ACTIVE')"
                    >
                      <UnlockOutlined />
                      解锁用户
                    </a-menu-item>
                    <a-menu-item
                      @click="handleDelete(record)"
                    >
                      <DeleteOutlined />
                      删除用户
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
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
            <a-form-item label="用户名" name="username">
              <a-input
                v-model:value="form.username"
                placeholder="请输入用户名"
                :disabled="isEdit"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="真实姓名" name="realName">
              <a-input
                v-model:value="form.realName"
                placeholder="请输入真实姓名"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="邮箱" name="email">
              <a-input
                v-model:value="form.email"
                placeholder="请输入邮箱"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号" name="phone">
              <a-input
                v-model:value="form.phone"
                placeholder="请输入手机号"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16" v-if="!isEdit">
          <a-col :span="12">
            <a-form-item label="密码" name="password">
              <a-input-password
                v-model:value="form.password"
                placeholder="请输入密码"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="确认密码" name="confirmPassword">
              <a-input-password
                v-model:value="form.confirmPassword"
                placeholder="请确认密码"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="头像URL" name="avatarUrl">
              <a-input
                v-model:value="form.avatarUrl"
                placeholder="请输入头像URL"
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
                <a-select-option value="INACTIVE">未激活</a-select-option>
                <a-select-option value="LOCKED">已锁定</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 重置密码模态框 -->
    <a-modal
      v-model:open="resetPasswordModalVisible"
      title="重置密码"
      width="400px"
      @ok="handleResetPasswordOk"
      @cancel="handleResetPasswordCancel"
    >
      <a-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        layout="vertical"
      >
        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="resetPasswordForm.newPassword"
            placeholder="请输入新密码"
          />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="resetPasswordForm.confirmPassword"
            placeholder="请确认新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  UserOutlined,
  DownOutlined,
  LockOutlined,
  UnlockOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus,
  resetPassword
} from '@/api/user'

// 响应式数据
const searchForm = reactive({
  username: '',
  realName: '',
  status: undefined
})

const form = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  avatarUrl: '',
  status: 'ACTIVE'
})

const resetPasswordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const tableData = ref([])
const tableLoading = ref(false)
const searchLoading = ref(false)
const modalVisible = ref(false)
const resetPasswordModalVisible = ref(false)
const isEdit = ref(false)

const formRef = ref()
const resetPasswordFormRef = ref()

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
    title: '头像',
    dataIndex: 'avatar',
    key: 'avatar',
    width: 80
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 120
  },
  {
    title: '真实姓名',
    dataIndex: 'realName',
    key: 'realName',
    width: 120
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 200
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 130
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '最后登录时间',
    dataIndex: 'lastLoginTime',
    key: 'lastLoginTime',
    width: 180
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
  username: [
    { required: true, message: '请输入用户名!' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名!' },
    { max: 50, message: '真实姓名不能超过 50 个字符' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确!' }
  ],
  password: [
    { required: true, message: '请输入密码!' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码!' },
    {
      validator: (rule, value) => {
        if (value !== form.password) {
          return Promise.reject('两次输入的密码不一致!')
        }
        return Promise.resolve()
      }
    }
  ]
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码!' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码!' },
    {
      validator: (rule, value) => {
        if (value !== resetPasswordForm.newPassword) {
          return Promise.reject('两次输入的密码不一致!')
        }
        return Promise.resolve()
      }
    }
  ]
}

// 计算属性
const modalTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// 方法
const fetchTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }

    // 添加搜索条件
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.realName) {
      params.realName = searchForm.realName
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }

    const response = await getUserList(params)
    tableData.value = response.data || []
    pagination.total = response.pagination?.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败')
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
    username: '',
    realName: '',
    status: undefined
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
    username: '',
    realName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    avatarUrl: '',
    status: 'ACTIVE'
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()

    const submitData = { ...form }
    if (!isEdit.value) {
      delete submitData.id
      delete submitData.confirmPassword
    } else {
      delete submitData.password
      delete submitData.confirmPassword
    }

    if (isEdit.value) {
      await updateUser(form.id, submitData)
      message.success('更新成功')
    } else {
      await createUser(submitData)
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

const handleResetPassword = (record) => {
  resetPasswordForm.userId = record.id
  resetPasswordModalVisible.value = true
}

const handleResetPasswordOk = async () => {
  try {
    await resetPasswordFormRef.value.validate()

    await resetPassword(resetPasswordForm.userId, {
      newPassword: resetPasswordForm.newPassword
    })

    message.success('密码重置成功')
    resetPasswordModalVisible.value = false
    Object.assign(resetPasswordForm, {
      userId: null,
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    console.error('重置密码失败:', error)
    message.error('重置密码失败')
  }
}

const handleResetPasswordCancel = () => {
  resetPasswordModalVisible.value = false
  Object.assign(resetPasswordForm, {
    userId: null,
    newPassword: '',
    confirmPassword: ''
  })
}

const handleUpdateStatus = (userId, status) => {
  Modal.confirm({
    title: '确认操作',
    content: `确定要${status === 'LOCKED' ? '锁定' : '解锁'}该用户吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await updateUserStatus(userId, status)
        message.success('操作成功')
        fetchTableData()
      } catch (error) {
        console.error('更新用户状态失败:', error)
        message.error('操作失败')
      }
    }
  })
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户"${record.username}"吗？此操作不可恢复！`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteUser(record.id)
        message.success('删除成功')
        fetchTableData()
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除失败')
      }
    }
  })
}

const getStatusColor = (status) => {
  const colors = {
    'ACTIVE': 'green',
    'INACTIVE': 'orange',
    'LOCKED': 'red'
  }
  return colors[status] || 'default'
}

// 生命周期
onMounted(() => {
  fetchTableData()
})
</script>

<style scoped lang="less">
.user-management {
  .search-card,
  .action-card,
  .table-card {
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }
}

@media (max-width: 768px) {
  .user-management {
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }

    :deep(.ant-table) {
      font-size: 12px;
    }
  }
}
</style>