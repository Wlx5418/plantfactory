import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加请求时间戳
    config.metadata = { startTime: new Date() }

    return config
  },
  error => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { config, data } = response

    // 计算请求耗时
    if (config.metadata) {
      const endTime = new Date()
      const duration = endTime.getTime() - config.metadata.startTime.getTime()
      console.log(`API请求耗时: ${config.url} - ${duration}ms`)
    }

    // 检查业务状态码
    if (data.code === 200 || data.code === 201) {
      return data.data
    } else {
      // 业务错误
      const errorMessage = data.message || '请求失败'
      message.error(errorMessage)
      return Promise.reject(new Error(errorMessage))
    }
  },
  error => {
    console.error('请求错误:', error)

    // 处理HTTP状态码
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token')
          const userStore = useUserStore()
          userStore.logoutAction()
          message.error('登录已过期，请重新登录')
          // 在实际应用中可以使用router跳转
          window.location.href = '/login'
          break

        case 403:
          message.error('没有权限访问该资源')
          break

        case 404:
          message.error('请求的资源不存在')
          break

        case 500:
          message.error('服务器内部错误')
          break

        default:
          const errorMessage = data?.message || error.message || '网络错误'
          message.error(errorMessage)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message.error('网络连接失败，请检查网络设置')
    } else {
      // 其他错误
      message.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default request