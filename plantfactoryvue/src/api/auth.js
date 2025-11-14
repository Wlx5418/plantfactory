import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 刷新令牌
export function refreshToken() {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 验证令牌
export function validateToken() {
  return request({
    url: '/auth/validate',
    method: 'post'
  })
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}