import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 根据ID获取用户详情
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

// 更新当前用户信息
export function updateCurrentUser(data) {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/users/change-password',
    method: 'put',
    data
  })
}

// 重置密码（管理员）
export function resetPassword(id, data) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'put',
    data
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 删除用户（软删除）
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 恢复删除的用户
export function restoreUser(id) {
  return request({
    url: `/users/${id}/restore`,
    method: 'put'
  })
}

// 批量删除用户
export function batchDeleteUsers(userIds) {
  return request({
    url: '/users/batch',
    method: 'delete',
    data: { userIds }
  })
}

// 获取用户状态选项
export function getUserStatusOptions() {
  return request({
    url: '/users/status-options',
    method: 'get'
  })
}

// 搜索用户
export function searchUsers(params) {
  return request({
    url: '/users/search',
    method: 'get',
    params
  })
}