import request from '@/utils/request'

// 创建生产区域
export function createProductionArea(data) {
  return request({
    url: '/production-areas',
    method: 'post',
    data
  })
}

// 获取生产区域列表
export function getProductionAreaList(params) {
  return request({
    url: '/production-areas',
    method: 'get',
    params
  })
}

// 根据ID获取生产区域详情
export function getProductionAreaById(id) {
  return request({
    url: `/production-areas/${id}`,
    method: 'get'
  })
}

// 更新生产区域信息
export function updateProductionArea(id, data) {
  return request({
    url: `/production-areas/${id}`,
    method: 'put',
    data
  })
}

// 删除生产区域
export function deleteProductionArea(id) {
  return request({
    url: `/production-areas/${id}`,
    method: 'delete'
  })
}

// 获取区域使用情况
export function getAreaUsage(id) {
  return request({
    url: `/production-areas/${id}/usage`,
    method: 'get'
  })
}

// 搜索生产区域
export function searchProductionAreas(params) {
  return request({
    url: '/production-areas/search',
    method: 'get',
    params
  })
}

// 获取生产区域统计信息
export function getAreaStatistics() {
  return request({
    url: '/production-areas/statistics',
    method: 'get'
  })
}

// 更新区域容量
export function updateAreaCapacity(id, capacity) {
  return request({
    url: `/production-areas/${id}/capacity`,
    method: 'put',
    data: { capacity }
  })
}

// 获取可用区域列表
export function getAvailableAreas() {
  return request({
    url: '/production-areas/available',
    method: 'get'
  })
}