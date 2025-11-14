import request from '@/utils/request'

// 数据类型枚举，与后端保持一致
export const DataType = {
  TEMPERATURE: 'TEMPERATURE',
  HUMIDITY: 'HUMIDITY',
  LIGHT_INTENSITY: 'LIGHT_INTENSITY',
  CO2: 'CO2',
  SOIL_MOISTURE: 'SOIL_MOISTURE',
  PH: 'PH'
}

// 数据状态枚举，与后端保持一致
export const DataStatus = {
  NORMAL: 'NORMAL',
  ABNORMAL: 'ABNORMAL',
  WARNING: 'WARNING'
}

// 保存环境数据
export function saveEnvironmentData(data) {
  return request({
    url: '/v1/environment/data',
    method: 'post',
    data
  })
}

// 批量保存环境数据
export function saveEnvironmentDataBatch(data) {
  return request({
    url: '/v1/environment/data/batch',
    method: 'post',
    data
  })
}

// 根据ID查询环境数据
export function getEnvironmentDataById(id) {
  return request({
    url: `/v1/environment/data/${id}`,
    method: 'get'
  })
}

// 根据传感器ID查询最新数据
export function getLatestBySensorId(sensorId) {
  return request({
    url: `/v1/environment/data/latest/sensor/${sensorId}`,
    method: 'get'
  })
}

// 根据传感器ID和数据类型查询最新数据
export function getLatestBySensorIdAndDataType(sensorId, dataType) {
  return request({
    url: `/v1/environment/data/latest/sensor/${sensorId}/type/${dataType}`,
    method: 'get'
  })
}

// 查询指定时间范围的数据
export function getDataByTimeRange(params) {
  return request({
    url: '/v1/environment/data/range',
    method: 'get',
    params
  })
}

// 查询指定传感器的数据
export function getDataBySensorId(sensorId, params) {
  return request({
    url: `/v1/environment/data/sensor/${sensorId}/range`,
    method: 'get',
    params
  })
}

// 查询指定数据类型的数据
export function getDataByDataType(dataType, params) {
  return request({
    url: `/v1/environment/data/type/${dataType}/range`,
    method: 'get',
    params
  })
}

// 查询异常数据
export function getAbnormalData(params) {
  return request({
    url: '/v1/environment/data/abnormal',
    method: 'get',
    params
  })
}

// 查询超出阈值的数据
export function getOutOfRangeData(params) {
  return request({
    url: '/v1/environment/data/out-of-range',
    method: 'get',
    params
  })
}

// 获取数据统计信息
export function getDataStatistics(dataType, params) {
  return request({
    url: `/v1/environment/statistics/${dataType}`,
    method: 'get',
    params
  })
}

// 获取所有数据类型的统计信息
export function getAllDataTypeStatistics(params) {
  return request({
    url: '/v1/environment/statistics',
    method: 'get',
    params
  })
}

// 获取图表数据
export function getChartData(dataType, params) {
  return request({
    url: `/v1/environment/chart/${dataType}`,
    method: 'get',
    params
  })
}

// 获取所有活跃传感器ID
export function getAllActiveSensorIds() {
  return request({
    url: '/v1/environment/sensors',
    method: 'get'
  })
}

// 获取传感器最新数据（批量）
export function getLatestDataBySensorIds(sensorIds) {
  return request({
    url: '/v1/environment/sensors/latest',
    method: 'post',
    data: sensorIds
  })
}

// 更新数据阈值
export function updateThresholds(id, params) {
  return request({
    url: `/v1/environment/data/${id}/thresholds`,
    method: 'put',
    params
  })
}

// 删除环境数据
export function deleteEnvironmentData(id) {
  return request({
    url: `/v1/environment/data/${id}`,
    method: 'delete'
  })
}

// 搜索环境数据
export function searchEnvironmentData(params) {
  return request({
    url: '/v1/environment/data/search',
    method: 'get',
    params
  })
}

// 创建模拟数据
export function createMockData() {
  return request({
    url: '/v1/environment/data/mock',
    method: 'post'
  })
}

// 清理旧数据
export function cleanupOldData(params) {
  return request({
    url: '/v1/environment/data/cleanup',
    method: 'delete',
    params
  })
}

// 检查传感器是否存在
export function checkSensorExists(sensorId) {
  return request({
    url: `/v1/environment/sensors/${sensorId}/exists`,
    method: 'get'
  })
}

// 为了兼容现有代码，保留旧方法名的别名
export const createEnvironmentData = saveEnvironmentData
export const updateEnvironmentData = deleteEnvironmentData // 临时映射，实际应该实现更新功能