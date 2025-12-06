<template>
  <div class="profile-page">
    <page-header title="个人信息" subtitle="查看和管理个人账户信息" />

    <div class="content-container">
      <a-row :gutter="24">
        <!-- 个人信息卡片 -->
        <a-col :xs="24" :lg="8">
          <a-card class="profile-card" :bordered="false">
            <div class="profile-header">
              <a-avatar :size="80" :src="userInfo.avatar">
                {{ userInfo.name.charAt(0) }}
              </a-avatar>
              <div class="profile-info">
                <h3>{{ userInfo.name }}</h3>
                <p class="role">{{ userInfo.role }}</p>
                <p class="department">{{ userInfo.department }}</p>
              </div>
            </div>

            <a-divider />

            <div class="profile-stats">
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.loginCount }}</div>
                <div class="stat-label">登录次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.onlineDays }}</div>
                <div class="stat-label">在线天数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.operationCount }}</div>
                <div class="stat-label">操作次数</div>
              </div>
            </div>

            <a-divider />

            <div class="profile-status">
              <div class="status-item">
                <span class="label">账户状态:</span>
                <a-tag color="green">正常</a-tag>
              </div>
              <div class="status-item">
                <span class="label">最后登录:</span>
                <span class="value">{{ userInfo.lastLoginTime }}</span>
              </div>
              <div class="status-item">
                <span class="label">注册时间:</span>
                <span class="value">{{ userInfo.createTime }}</span>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 详细信息和表单 -->
        <a-col :xs="24" :lg="16">
          <!-- 基本信息表单 -->
          <a-card title="基本信息" class="form-card" :bordered="false">
            <a-form
              ref="basicFormRef"
              :model="basicForm"
              layout="vertical"
              @finish="handleBasicInfoUpdate"
            >
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item label="用户名" name="username">
                    <a-input v-model:value="basicForm.username" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="姓名" name="name" :rules="[{ required: true, message: '请输入姓名' }]">
                    <a-input v-model:value="basicForm.name" placeholder="请输入姓名" />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item label="邮箱" name="email" :rules="[
                    { required: true, message: '请输入邮箱' },
                    { type: 'email', message: '请输入有效的邮箱地址' }
                  ]">
                    <a-input v-model:value="basicForm.email" placeholder="请输入邮箱" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="手机号" name="phone" :rules="[
                    { required: true, message: '请输入手机号' },
                    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
                  ]">
                    <a-input v-model:value="basicForm.phone" placeholder="请输入手机号" />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item label="部门" name="department">
                    <a-select v-model:value="basicForm.department" placeholder="请选择部门">
                      <a-select-option value="技术部">技术部</a-select-option>
                      <a-select-option value="生产部">生产部</a-select-option>
                      <a-select-option value="运维部">运维部</a-select-option>
                      <a-select-option value="管理部">管理部</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="职位" name="position">
                    <a-input v-model:value="basicForm.position" placeholder="请输入职位" />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-form-item>
                <a-button type="primary" html-type="submit" :loading="updateLoading">
                  更新基本信息
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 修改密码 -->
          <a-card title="修改密码" class="form-card" :bordered="false">
            <a-form
              ref="passwordFormRef"
              :model="passwordForm"
              layout="vertical"
              @finish="handlePasswordUpdate"
            >
              <a-form-item label="当前密码" name="oldPassword" :rules="[
                { required: true, message: '请输入当前密码' }
              ]">
                <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码" />
              </a-form-item>

              <a-form-item label="新密码" name="newPassword" :rules="[
                { required: true, message: '请输入新密码' },
                { min: 6, message: '密码长度不能少于6位' }
              ]">
                <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
              </a-form-item>

              <a-form-item label="确认新密码" name="confirmPassword" :rules="[
                { required: true, message: '请确认新密码' },
                { validator: validateConfirmPassword }
              ]">
                <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请确认新密码" />
              </a-form-item>

              <a-form-item>
                <a-button type="primary" html-type="submit" :loading="passwordLoading">
                  修改密码
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
      </a-row>

      <!-- 最近活动 -->
      <a-card title="最近活动" class="activity-card" :bordered="false">
        <a-timeline>
          <a-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :color="activity.color"
          >
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-description">{{ activity.description }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'

// 用户信息
const userInfo = ref({
  id: 1,
  username: 'admin',
  name: '系统管理员',
  email: 'admin@plantfactory.com',
  phone: '13800138000',
  department: '技术部',
  position: '系统管理员',
  role: '超级管理员',
  avatar: '',
  loginCount: 156,
  onlineDays: 45,
  operationCount: 1234,
  lastLoginTime: '2024-11-25 10:30:00',
  createTime: '2024-01-01 09:00:00'
})

// 表单状态
const updateLoading = ref(false)
const passwordLoading = ref(false)
const basicFormRef = ref()
const passwordFormRef = ref()

// 基本信息表单
const basicForm = reactive({
  username: 'admin',
  name: '系统管理员',
  email: 'admin@plantfactory.com',
  phone: '13800138000',
  department: '技术部',
  position: '系统管理员'
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '登录系统',
    description: '通过Web端登录系统',
    time: '2024-11-25 10:30:00',
    color: 'green'
  },
  {
    id: 2,
    title: '修改环境参数',
    description: '调整了A区的温度和湿度参数',
    time: '2024-11-25 09:15:00',
    color: 'blue'
  },
  {
    id: 3,
    title: '导出数据报告',
    description: '导出了7天的环境数据报告',
    time: '2024-11-24 16:45:00',
    color: 'orange'
  },
  {
    id: 4,
    title: '设备控制操作',
    description: '远程控制了灌溉系统的开关',
    time: '2024-11-24 14:20:00',
    color: 'purple'
  },
  {
    id: 5,
    title: '查看历史数据',
    description: '查看了最近30天的环境数据趋势',
    time: '2024-11-24 11:30:00',
    color: 'cyan'
  }
])

// 确认密码验证
const validateConfirmPassword = async (rule, value) => {
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 更新基本信息
const handleBasicInfoUpdate = async () => {
  try {
    updateLoading.value = true

    // 模拟API调用
    setTimeout(() => {
      Object.assign(userInfo.value, basicForm)
      updateLoading.value = false
      message.success('基本信息更新成功')
    }, 1000)
  } catch (error) {
    updateLoading.value = false
    message.error('更新失败，请重试')
  }
}

// 修改密码
const handlePasswordUpdate = async () => {
  try {
    passwordLoading.value = true

    // 模拟API调用
    setTimeout(() => {
      passwordLoading.value = false
      message.success('密码修改成功，请重新登录')

      // 重置表单
      Object.assign(passwordForm, {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      passwordFormRef.value.resetFields()
    }, 1000)
  } catch (error) {
    passwordLoading.value = false
    message.error('密码修改失败，请重试')
  }
}

// 组件挂载
onMounted(() => {
  // 初始化表单数据
  Object.assign(basicForm, {
    username: userInfo.value.username,
    name: userInfo.value.name,
    email: userInfo.value.email,
    phone: userInfo.value.phone,
    department: userInfo.value.department,
    position: userInfo.value.position
  })
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header {
  text-align: center;
  margin-bottom: 16px;
}

.profile-info {
  margin-top: 16px;
}

.profile-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.profile-info .role {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
}

.profile-info .department {
  margin: 0;
  font-size: 14px;
  color: #8c8c8c;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin: 16px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

.profile-status {
  margin-top: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.status-item .label {
  color: #595959;
}

.status-item .value {
  color: #262626;
}

.form-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.activity-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.activity-content {
  padding-left: 8px;
}

.activity-title {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.activity-description {
  color: #595959;
  font-size: 14px;
  margin-bottom: 4px;
}

.activity-time {
  color: #8c8c8c;
  font-size: 12px;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }

  .profile-stats {
    flex-direction: column;
    gap: 16px;
  }

  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>