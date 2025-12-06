<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <div class="login-header">
        <div class="logo">
          <img src="@/assets/logo.svg" alt="Logo" />
        </div>
        <h1 class="title">植物工厂管理系统</h1>
        <p class="subtitle">智能农业管理平台</p>
      </div>

      <a-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        class="login-form"
        @finish="handleLogin"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="loginForm.username"
            size="large"
            placeholder="请输入用户名"
            :prefix="h(UserOutlined)"
          />
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="loginForm.password"
            size="large"
            placeholder="请输入密码"
            :prefix="h(LockOutlined)"
            @pressEnter="() => loginFormRef.value?.submit()"
          />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="loginForm.remember">
            记住密码
          </a-checkbox>
          <a class="login-form-forgot" href="">忘记密码</a>
        </a-form-item>

        <a-form-item>
          <a-button
            type="default"
            size="large"
            block
            @click="goToRegister"
          >
            注册账号
          </a-button>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            size="large"
            :loading="loading"
            block
            html-type="submit"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <p>默认管理员账号: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const loginFormRef = ref()

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名!' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符' }
  ],
  password: [
    { required: true, message: '请输入密码!' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符' }
  ]
}

const handleLogin = async (values) => {
  // 设置loading状态
  loading.value = true

  try {
    // @finish事件会自动验证表单并传递values参数
    const formData = {
      username: values.username,
      password: values.password,
      remember: loginForm.remember
    }

    // 处理记住密码
    if (loginForm.remember) {
      localStorage.setItem('remembered_username', values.username)
      localStorage.setItem('remembered_password', values.password)
      localStorage.setItem('remember_password', 'true')
    } else {
      // 如果不记住密码，清除保存的信息
      localStorage.removeItem('remembered_username')
      localStorage.removeItem('remembered_password')
      localStorage.removeItem('remember_password')
    }

    // 调用登录API
    const response = await userStore.loginAction(formData)

    // 检查登录响应 - axios拦截器已经处理了成功的情况，直接返回data
    if (response && response.accessToken) {
      message.success('登录成功')

      // 如果有重定向地址，跳转到重定向地址
      const redirect = route.query.redirect || '/dashboard'
      router.push(redirect)
    } else {
      throw new Error('登录失败')
    }

  } catch (error) {
    console.error('登录失败:', error)

    // 处理各种错误情况
    let errorMessage = '登录失败，请检查用户名和密码'

    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }

    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 组件挂载时检查是否已登录，并填充记住的密码
onMounted(async () => {
  // 加载记住的用户名和密码
  const savedUsername = localStorage.getItem('remembered_username')
  const savedPassword = localStorage.getItem('remembered_password')
  const shouldRemember = localStorage.getItem('remember_password') === 'true'

  if (savedUsername) {
    loginForm.username = savedUsername
  }
  if (savedPassword && shouldRemember) {
    loginForm.password = savedPassword
    loginForm.remember = true
  }

  // 检查是否已登录
  if (userStore.isLoggedIn) {
    await userStore.initAuth()
    router.push('/dashboard')
  }
})
</script>

<style scoped lang="less">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('@/assets/login-bg.jpg') center/cover;
    opacity: 0.1;
    z-index: 0;
  }
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  z-index: 1;
  position: relative;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    margin-bottom: 16px;

    img {
      width: 64px;
      height: 64px;
    }
  }

  .title {
    font-size: 28px;
    font-weight: 600;
    color: #1890ff;
    margin-bottom: 8px;
    margin-top: 0;
  }

  .subtitle {
    color: #666;
    font-size: 14px;
    margin: 0;
  }
}

.login-form {
  .ant-form-item {
    margin-bottom: 24px;
  }

  .ant-input-affix-wrapper,
  .ant-input {
    height: 48px;
    border-radius: 6px;
  }

  .ant-input-prefix {
    color: rgba(0, 0, 0, 0.45);
  }
}

.login-form-forgot {
  float: right;
  color: #1890ff;

  &:hover {
    color: #40a9ff;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  p {
    color: #666;
    font-size: 12px;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-form-wrapper {
    margin: 0 20px;
    padding: 30px 20px;
  }

  .login-header {
    .title {
      font-size: 24px;
    }
  }
}
</style>