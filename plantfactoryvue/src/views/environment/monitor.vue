<template>
  <div class="environment-monitor">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">环境实时监控</h1>
        <p class="page-description">监控植物工厂的环境参数，确保最佳生长条件</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" :icon="h(ReloadOutlined)" :loading="loading" @click="refreshData">
            刷新数据
          </a-button>
          <a-button :icon="h(SettingOutlined)" @click="showSettings = true">
            监控设置
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 实时数据卡片 -->
    <a-row :gutter="[16, 16]" class="data-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <EnvironmentDataCard
          title="温度"
          :value="currentData.temperature"
          unit="°C"
          :status="getStatus(currentData.temperature, 'temperature')"
          icon="thermometer"
          :trend="temperatureTrend"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <EnvironmentDataCard
          title="湿度"
          :value="currentData.humidity"
          unit="%"
          :status="getStatus(currentData.humidity, 'humidity')"
          icon="water"
          :trend="humidityTrend"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <EnvironmentDataCard
          title="光照强度"
          :value="currentData.lightIntensity"
          unit="lux"
          :status="getStatus(currentData.lightIntensity, 'light')"
          icon="sun"
          :trend="lightTrend"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <EnvironmentDataCard
          title="CO₂浓度"
          :value="currentData.co2Level"
          unit="ppm"
          :status="getStatus(currentData.co2Level, 'co2')"
          icon="cloud"
          :trend="co2Trend"
        />
      </a-col>
    </a-row>

    <!-- 监控面板 -->
    <a-row :gutter="[16, 16]" class="monitoring-panels">
      <!-- 实时趋势图 -->
      <a-col :xs="24" :lg="16">
        <a-card title="实时趋势图" :bordered="false" class="chart-card">
          <div class="chart-controls">
            <a-radio-group v-model:value="timeRange" @change="handleTimeRangeChange">
              <a-radio-button value="1h">1小时</a-radio-button>
              <a-radio-button value="6h">6小时</a-radio-button>
              <a-radio-button value="24h">24小时</a-radio-button>
              <a-radio-button value="7d">7天</a-radio-button>
            </a-radio-group>
          </div>
          <div class="chart-container">
            <RealtimeChart
              :data="chartData"
              :timeRange="timeRange"
              :loading="chartLoading"
            />
          </div>
        </a-card>
      </a-col>

      <!-- 控制面板 -->
      <a-col :xs="24" :lg="8">
        <a-card title="设备控制" :bordered="false" class="control-panel">
          <DeviceControl
            :devices="devices"
            @control="handleDeviceControl"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 分区监控 -->
    <a-card title="分区监控" :bordered="false" class="zone-monitoring">
      <a-tabs v-model:activeKey="activeZoneTab" type="card" @change="handleZoneChange">
        <a-tab-pane
          v-for="zone in zones"
          :key="zone.id"
          :tab="zone.name"
        >
          <div class="zone-content">
            <div class="zone-info">
              <a-descriptions :column="4" size="small">
                <a-descriptions-item label="当前状态">
                  <a-badge :status="zone.status === 'normal' ? 'success' : 'warning'" :text="zone.statusText" />
                </a-descriptions-item>
                <a-descriptions-item label="温度">{{ zone.temperature }}°C</a-descriptions-item>
                <a-descriptions-item label="湿度">{{ zone.humidity }}%</a-descriptions-item>
                <a-descriptions-item label="光照">{{ zone.lightIntensity }}lux</a-descriptions-item>
              </a-descriptions>
            </div>
            <div class="zone-alerts" v-if="zone.alerts && zone.alerts.length > 0">
              <a-alert
                v-for="alert in zone.alerts"
                :key="alert.id"
                :message="alert.message"
                :type="alert.type"
                show-icon
                class="zone-alert"
              />
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 告警信息 -->
    <a-card title="系统告警" :bordered="false" class="alerts-panel">
      <a-list
        :data-source="alerts"
        :loading="alertsLoading"
        size="small"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <a-badge :status="getAlertStatus(item.level)" />
              </template>
              <template #title>
                <span :class="['alert-title', `alert-${item.level}`]">{{ item.title }}</span>
              </template>
              <template #description>
                {{ item.message }} - {{ formatTime(item.timestamp) }}
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" size="small" @click="handleAlert(item)">
                处理
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 设置弹窗 -->
    <a-modal
      v-model:open="showSettings"
      title="监控设置"
      :footer="null"
      width="600px"
    >
      <MonitorSettings
        :settings="monitorSettings"
        @save="saveSettings"
        @cancel="showSettings = false"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, h, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, SettingOutlined } from '@ant-design/icons-vue'
import EnvironmentDataCard from './components/EnvironmentDataCard.vue'
import RealtimeChart from './components/RealtimeChart.vue'
import DeviceControl from './components/DeviceControl.vue'
import MonitorSettings from './components/MonitorSettings.vue'
import { useWebSocket } from '@/composables/useWebSocket'

// WebSocket 连接
const { connect, disconnect, subscribe, unsubscribe } = useWebSocket()

// 响应式数据
const loading = ref(false)
const chartLoading = ref(false)
const alertsLoading = ref(false)
const showSettings = ref(false)
const timeRange = ref('1h')
const activeZoneTab = ref('zone1')

// 当前环境数据
const currentData = reactive({
  temperature: 25.5,
  humidity: 75,
  lightIntensity: 5500,
  co2Level: 800,
  timestamp: new Date()
})

// 历史数据趋势
const temperatureTrend = ref([25.2, 25.3, 25.5, 25.4, 25.5])
const humidityTrend = ref([74, 75, 76, 75, 75])
const lightTrend = ref([5200, 5300, 5500, 5400, 5500])
const co2Trend = ref([780, 790, 800, 795, 800])

// 图表数据
const chartData = ref([])

// 设备列表
const devices = ref([
  {
    id: 'fan1',
    name: '通风系统',
    type: 'fan',
    status: 'running',
    autoMode: true,
    settings: {
      speed: 60,
      threshold: 28
    }
  },
  {
    id: 'light1',
    name: 'LED补光灯',
    type: 'light',
    status: 'on',
    autoMode: true,
    settings: {
      brightness: 80,
      schedule: '06:00-22:00'
    }
  },
  {
    id: 'pump1',
    name: '水泵系统',
    type: 'pump',
    status: 'running',
    autoMode: true,
    settings: {
      flowRate: 50,
      interval: 30
    }
  }
])

// 分区信息
const zones = ref([
  {
    id: 'zone1',
    name: '生长区A',
    status: 'normal',
    statusText: '正常运行',
    temperature: 25.5,
    humidity: 75,
    lightIntensity: 5500,
    co2Level: 800,
    alerts: []
  },
  {
    id: 'zone2',
    name: '生长区B',
    status: 'warning',
    statusText: '温度偏高',
    temperature: 28.5,
    humidity: 65,
    lightIntensity: 6200,
    co2Level: 750,
    alerts: [
      {
        id: 'alert1',
        type: 'warning',
        message: '温度超出正常范围，建议降低温度或增加通风'
      }
    ]
  },
  {
    id: 'zone3',
    name: '育苗区',
    status: 'normal',
    statusText: '正常运行',
    temperature: 24.8,
    humidity: 80,
    lightIntensity: 4800,
    co2Level: 850,
    alerts: []
  }
])

// 告警列表
const alerts = ref([
  {
    id: 1,
    level: 'warning',
    title: '温度告警',
    message: '生长区B温度达到28.5°C，超出正常范围',
    timestamp: new Date(Date.now() - 10 * 60 * 1000)
  },
  {
    id: 2,
    level: 'info',
    title: '设备状态',
    message: 'LED补光灯已自动开启',
    timestamp: new Date(Date.now() - 30 * 60 * 1000)
  }
])

// 监控设置
const monitorSettings = reactive({
  temperature: { min: 20, max: 28 },
  humidity: { min: 60, max: 80 },
  lightIntensity: { min: 3000, max: 8000 },
  co2Level: { min: 600, max: 1200 },
  alertInterval: 5,
  dataInterval: 30
})

// 方法
const getStatus = (value, type) => {
  const thresholds = monitorSettings[type]
  if (value < thresholds.min) return 'low'
  if (value > thresholds.max) return 'high'
  return 'normal'
}

const getAlertStatus = (level) => {
  const statusMap = {
    'error': 'error',
    'warning': 'warning',
    'info': 'processing'
  }
  return statusMap[level] || 'default'
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const refreshData = async () => {
  loading.value = true
  try {
    // 模拟数据刷新
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 更新当前数据
    currentData.temperature = 25 + Math.random() * 3
    currentData.humidity = 70 + Math.random() * 10
    currentData.lightIntensity = 5000 + Math.random() * 2000
    currentData.co2Level = 750 + Math.random() * 100
    currentData.timestamp = new Date()

    // 更新趋势数据
    temperatureTrend.value.push(currentData.temperature)
    humidityTrend.value.push(currentData.humidity)
    lightTrend.value.push(currentData.lightIntensity)
    co2Trend.value.push(currentData.co2Level)

    if (temperatureTrend.value.length > 20) {
      temperatureTrend.value.shift()
      humidityTrend.value.shift()
      lightTrend.value.shift()
      co2Trend.value.shift()
    }

    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const handleTimeRangeChange = (e) => {
  chartLoading.value = true
  // 根据时间范围更新图表数据
  setTimeout(() => {
    chartLoading.value = false
  }, 500)
}

const handleDeviceControl = async (deviceId, action) => {
  try {
    const device = devices.value.find(d => d.id === deviceId)
    if (action === 'toggle') {
      device.status = device.status === 'on' ? 'off' : 'on'
    } else if (action === 'mode') {
      device.autoMode = !device.autoMode
    }
    message.success(`设备控制成功`)
  } catch (error) {
    message.error('设备控制失败')
  }
}

const handleZoneChange = (zoneId) => {
  const zone = zones.value.find(z => z.id === zoneId)
  if (zone) {
    // 加载分区详细数据
    console.log('加载分区数据:', zone.name)
  }
}

const handleAlert = (alert) => {
  // 处理告警
  const index = alerts.value.findIndex(a => a.id === alert.id)
  if (index > -1) {
    alerts.value.splice(index, 1)
    message.success('告警已处理')
  }
}

const saveSettings = (newSettings) => {
  Object.assign(monitorSettings, newSettings)
  showSettings.value = false
  message.success('设置保存成功')
}

// WebSocket 消息处理
const handleWebSocketMessage = (message) => {
  const data = JSON.parse(message)

  if (data.type === 'environment_data') {
    // 更新实时数据
    Object.assign(currentData, data.payload)
  } else if (data.type === 'alert') {
    // 添加新告警
    alerts.value.unshift(data.payload)
  } else if (data.type === 'device_status') {
    // 更新设备状态
    const device = devices.value.find(d => d.id === data.payload.deviceId)
    if (device) {
      Object.assign(device, data.payload)
    }
  }
}

// 生命周期
onMounted(() => {
  // 建立 WebSocket 连接
  connect()
  subscribe('/topic/environment', handleWebSocketMessage)

  // 初始化数据
  refreshData()

  // 设置定时刷新
  const timer = setInterval(refreshData, 30000)

  onUnmounted(() => {
    clearInterval(timer)
    unsubscribe('/topic/environment')
    disconnect()
  })
})
</script>

<style scoped lang="less">
.environment-monitor {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
      .page-title {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: #262626;
      }

      .page-description {
        margin: 0;
        color: #8c8c8c;
        font-size: 14px;
      }
    }
  }

  .data-cards {
    margin-bottom: 24px;
  }

  .monitoring-panels {
    margin-bottom: 24px;

    .chart-card {
      .chart-controls {
        margin-bottom: 16px;
        text-align: right;
      }

      .chart-container {
        height: 400px;
      }
    }

    .control-panel {
      min-height: 400px;
    }
  }

  .zone-monitoring {
    margin-bottom: 24px;

    .zone-content {
      .zone-info {
        margin-bottom: 16px;
      }

      .zone-alerts {
        .zone-alert {
          margin-bottom: 8px;
        }
      }
    }
  }

  .alerts-panel {
    .alert-title {
      font-weight: 500;

      &.alert-error {
        color: #ff4d4f;
      }

      &.alert-warning {
        color: #faad14;
      }

      &.alert-info {
        color: #1890ff;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .environment-monitor {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .header-right {
        text-align: center;
      }
    }

    .chart-container {
      height: 300px !important;
    }
  }
}
</style>