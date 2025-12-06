import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  // 计算属性
  const isLoggedIn = computed(() => {
    const hasToken = !!token.value
    // 只要token存在就认为是登录状态，用户信息可以异步加载
    return hasToken
  })
  const hasPermission = computed(() => (permission) => {
    return permissions.value.includes(permission) || permissions.value.includes('ADMIN')
  })

  const hasRole = computed(() => (role) => {
    return userInfo.value?.role === role || permissions.value.includes('ADMIN')
  })
  const displayName = computed(() => {
    return userInfo.value?.realName || userInfo.value?.username || '未知用户'
  })

  // 方法
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setPermissions = (perms) => {
    permissions.value = perms
    localStorage.setItem('permissions', JSON.stringify(perms))
  }

  const loginAction = async (loginForm) => {
    try {
      const response = await login(loginForm)

      // axios拦截器已经处理了response.code === 200的情况，直接返回了data部分
      // 所以这里response就是后端返回的data对象
      if (response && response.accessToken) {
        const { accessToken: tokenValue, userInfo } = response

        // 设置token
        setToken(tokenValue)

        // 设置用户信息 - 修复数据解析问题
        const userRole = userInfo?.roles?.[0] || 'USER'
        setUserInfo({
          id: userInfo?.id,
          username: userInfo?.username,
          realName: userInfo?.realName,
          role: userRole,
          email: userInfo?.email || null,
          phone: userInfo?.phone || null
        })

        // 根据用户角色设置权限
        const userPermissions = ['USER']

        // 简化逻辑：admin用户直接给予ADMIN权限
        if (userInfo?.username === 'admin' || userInfo?.id === 1) {
          userPermissions.push('ADMIN')
        } else if (userRole === 'ADMIN') {
          userPermissions.push('ADMIN')
        } else if (userInfo?.roles) {
          // 检查角色编码是否为ADMIN
          const hasAdminRole = Array.from(userInfo.roles).some(role =>
            role.roleCode === 'ADMIN' || role.roleName === '系统管理员'
          )
          if (hasAdminRole) {
            userPermissions.push('ADMIN')
          }
        }

        setPermissions(userPermissions)
        return response
      } else {
        throw new Error('登录响应格式异常')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录请求失败:', error)
    } finally {
      // 清除本地存储
      clearUserData()
    }
  }

  const fetchUserInfo = async () => {
    try {
      const response = await getCurrentUser()
      if (response.code === 200) {
        setUserInfo(response.data)

        // 根据用户角色设置权限
        const userPermissions = ['USER']
        if (response.data.username === 'admin' || response.data.id === 1) {
          userPermissions.push('ADMIN')
        }

        setPermissions(userPermissions)
        return response.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  const clearUserData = () => {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
  }

  const updateUserInfo = (updatedInfo) => {
    setUserInfo(updatedInfo)
  }

  const checkAuth = () => {
    if (!token.value) {
      return false
    }

    // 可以在这里添加token有效性检查
    return true
  }

  // 初始化时检查认证状态 - 简化版本
  const initAuth = async () => {
    // 如果有token但用户信息为空，从localStorage恢复
    if (token.value && !userInfo.value) {
      const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      if (storedUserInfo) {
        userInfo.value = storedUserInfo
      }
    }

    // 如果有token但权限为空，从localStorage恢复
    if (token.value && permissions.value.length === 0) {
      const storedPermissions = JSON.parse(localStorage.getItem('permissions') || '[]')
      if (storedPermissions.length > 0) {
        permissions.value = storedPermissions
      } else {
        // 默认权限
        permissions.value = ['USER']
        localStorage.setItem('permissions', JSON.stringify(permissions.value))
      }
    }

    // 如果是admin用户且没有权限，默认设置管理员权限
    if (token.value && userInfo.value?.username === 'admin' && !permissions.value.includes('ADMIN')) {
      permissions.value = ['ADMIN', 'USER']
      localStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }

  return {
    // 状态
    token,
    userInfo,
    permissions,

    // 计算属性
    isLoggedIn,
    hasPermission,
    hasRole,
    displayName,

    // 方法
    setToken,
    setUserInfo,
    setPermissions,
    loginAction,
    logoutAction,
    fetchUserInfo,
    clearUserData,
    updateUserInfo,
    checkAuth,
    initAuth
  }
})