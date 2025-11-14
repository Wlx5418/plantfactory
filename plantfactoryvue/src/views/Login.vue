<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 主容器 -->
    <div class="login-main">
      <!-- 左侧介绍区域 -->
      <div class="login-intro">
        <div class="intro-content">
          <div class="intro-icon">🌱</div>
          <h1>植物工厂管理系统</h1>
          <p class="intro-subtitle">智能农业 · 精准管理 · 高效生产</p>
          <div class="intro-features">
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span>实时数据监控</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌡️</span>
              <span>环境智能控制</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📈</span>
              <span>产量优化分析</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-form-section">
        <div class="login-form-container">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账户以继续</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <div class="input-wrapper">
                <span class="input-icon">👤</span>
                <input
                  type="text"
                  id="username"
                  v-model="loginForm.username"
                  required
                  placeholder="用户名"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <span class="input-icon">🔒</span>
                <input
                  type="password"
                  id="password"
                  v-model="loginForm.password"
                  required
                  placeholder="密码"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" />
                <span>记住我</span>
              </label>
              <a href="#" class="forgot-password">忘记密码？</a>
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <div v-if="errorMessage" class="error-message">
            <span class="error-icon">⚠️</span>
            {{ errorMessage }}
          </div>

          <div class="demo-hint">
            <p>演示账号: admin / 123456</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')

const loginForm = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(loginForm.value)
    })

    const data = await response.json()

    if (data.code === 200) {
      localStorage.setItem('token', data.data.accessToken)
      localStorage.setItem('userInfo', JSON.stringify({
        id: data.data.userId,
        username: data.data.username,
        realName: data.data.realName
      }))
      router.push('/home')
    } else {
      errorMessage.value = data.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.bg-circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  right: -100px;
  animation-delay: 2s;
}

.bg-circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: -75px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 主容器 */
.login-main {
  display: flex;
  width: 900px;
  height: 600px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

/* 左侧介绍区域 */
.login-intro {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.intro-content {
  text-align: center;
}

.intro-icon {
  font-size: 60px;
  margin-bottom: 20px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.intro-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
  line-height: 1.2;
}

.intro-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.intro-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;
}

.feature-icon {
  font-size: 18px;
}

/* 右侧登录区域 */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-form-container {
  width: 100%;
  max-width: 350px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.form-header p {
  color: #666;
  font-size: 14px;
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  margin-bottom: 5px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  font-size: 16px;
  color: #999;
  z-index: 1;
}

.form-input {
  width: 100%;
  padding: 15px 15px 15px 45px;
  border: 2px solid #e1e5e9;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: #fafafa;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #999;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin: 10px 0;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #5a67d8;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #ffffff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  margin-top: 20px;
  padding: 12px 15px;
  background: #fee;
  color: #f66;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #fcc;
}

.error-icon {
  font-size: 16px;
}

.demo-hint {
  margin-top: 30px;
  padding: 15px;
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  border-radius: 8px;
  text-align: center;
}

.demo-hint p {
  color: #0369a1;
  font-size: 13px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-main {
    flex-direction: column;
    width: 95%;
    max-width: 400px;
    height: auto;
    max-height: 90vh;
  }

  .login-intro {
    padding: 30px 20px;
    min-height: 200px;
  }

  .intro-content h1 {
    font-size: 24px;
  }

  .intro-features {
    display: none;
  }

  .login-form-section {
    padding: 30px 20px;
  }

  .bg-circle {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 20px;
  }

  .login-main {
    width: 100%;
  }

  .form-header h2 {
    font-size: 24px;
  }

  .login-btn {
    padding: 12px;
    font-size: 15px;
  }
}
</style>