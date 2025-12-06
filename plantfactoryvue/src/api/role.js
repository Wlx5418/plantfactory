import request from '@/utils/request'

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: '/roles',
    method: 'get',
    params
  })
}

// 根据ID获取角色详情
export function getRoleById(id) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

// 批量删除角色
export function batchDeleteRoles(roleIds) {
  return request({
    url: '/roles/batch',
    method: 'delete',
    data: { roleIds }
  })
}

// 更新角色状态
export function updateRoleStatus(id, status) {
  return request({
    url: `/roles/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 获取角色权限列表
export function getRolePermissions(roleId) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'get'
  })
}

// 分配角色权限
export function assignRolePermissions(roleId, permissions) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'put',
    data: { permissions }
  })
}

// 获取所有权限选项
export function getPermissionOptions() {
  return request({
    url: '/permissions/options',
    method: 'get'
  })
}

// 获取角色用户数量
export function getRoleUserCount(roleId) {
  return request({
    url: `/roles/${roleId}/user-count`,
    method: 'get'
  })
}