<template>
  <div class="realtime-monitor">
    <!-- 连接状态指示器 -->
    <div class="connection-status">
      <a-badge :status="connectionStatus" :text="connectionText" />
      <a-button
        v-if="!isConnected"
        type="primary"
        size="small"
        @click="connect"
        :loading="connecting"
      >
        重新连接
      </a-button>
    </div>

    <!-- 实时数据卡片 -->
    <a-row :gutter="[16, 16]" class="mb-4">
      <a-col :span="6" v-for="metric in metrics" :key="metric.key">
        <a-card>
          <a-statistic
            :title="metric.title"
            :value="metric.value"
            :suffix="metric.unit"
            :value-style="{ color: metric.color }"
            :precision="metric.precision || 1"
          >
            <template #prefix>
              <component :is="metric.icon" />
            </template>
          </a-statistic>

          <!-- 状态指示器 -->
          <div class="metric-status" v-if="metric.status">
            <a-tag :color="metric.statusColor">{{ metric.statusText }}</a-tag>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 实时图表 -->
    <a-row :gutter="[16, 16]">
      <a-col :span="12">
        <a-card title="温度趋势" :loading="loading">
          <v-chart
            :option="temperatureChartOption"
            style="height: 300px;"
            autoresize
          />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="湿度趋势" :loading="loading">
          <v-chart
            :option="humidityChartOption"
            style="height: 300px;"
            autoresize
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 告警信息 -->
    <a-card title="系统告警" class="mt-4" v-if="alerts.length > 0">
      <a-list :data-source="alerts" item-layout="horizontal">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-tag :color="getAlertColor(item.severity)">{{ item.severity }}</a-tag>
                {{ item.alertType }}
              </template>
              <template #description>
                {{ item.message }}
                <br />
                <small class="text-gray-500">{{ formatTime(item.timestamp) }}</small>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 设备控制面板 -->
    <a-card title="设备控制" class="mt-4">
      <a-row :gutter="[16, 16]">
        <a-col :span="8" v-for="device in devices" :key="device.id">
          <a-card size="small" :title="device.name">
            <div class="device-controls">
              <a-space direction="vertical" style="width: 100%">
                <a-switch
                  v-model:checked="device.status"
                  @change="(checked) => toggleDevice(device.id, checked)"
                  :loading="device.loading"
                >
                  {{ device.status ? '开启' : '关闭' }}
                </a-switch>

                <a-slider
                  v-if="device.type === 'light' && device.status"
                  v-model:value="device.intensity"
                  :min="0"
                  :max="100"
                  @change="(value) => adjustDevice(device.id, { intensity: value })"
                  :tooltip-open="true"
                  :tooltip-formatter="(value) => `${value}%`"
                />

                <a-select
                  v-if="device.type === 'fan' && device.status"
                  v-model:value="device.speed"
                  @change="(value) => adjustDevice(device.id, { speed: value })"
                  style="width: 100%"
                >
                  <a-select-option value="low">低速</a-select-option>
                  <a-select-option value="medium">中速</a-select-option>
                  <a-select-option value="high">高速</a-select-option>
                </a-select>
              </a-space>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { message } from 'ant-design-vue'
import {
  ThermometerOutlined,
  EyeOutlined,
  BulbOutlined,
  CloudOutlined
} from '@ant-design/icons-vue'
import websocketManager from '@/utils/websocket'
import dayjs from 'dayjs'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

// Props
const props = defineProps({
  areaId: {
    type: Number,
    required: true
  }
})

// 响应式数据
const isConnected = ref(false)
const connecting = ref(false)
const loading = ref(false)
const alerts = ref([])
const realTimeData = ref([])
const devices = ref([
  { id: 1, name: '生长灯', type: 'light', status: false, intensity: 80, loading: false },
  { id: 2, name: '通风扇', type: 'fan', status: false, speed: 'medium', loading: false },
  { id: 3, name: '水泵', type: 'pump', status: false, loading: false }
])

// WebSocket订阅
let environmentSubscription = null
let alertSubscription = null

// 计算属性
const connectionStatus = computed(() => {
  return isConnected.value ? 'success' : 'error'
})

const connectionText = computed(() => {
  return isConnected.value ? '已连接' : '连接断开'
})

const metrics = computed(() => {
  if (realTimeData.value.length === 0) {
    return [
      { key: 'temperature', title: '温度', value: 0, unit: '°C', color: '#1890ff', icon: 'ThermometerOutlined' },
      { key: 'humidity', title: '湿度', value: 0, unit: '%', color: '#52c41a', icon: 'EyeOutlined' },
      { key: 'lightIntensity', title: '光照强度', value: 0, unit: 'lux', color: '#faad14', icon: 'BulbOutlined' },
      { key: 'co2Level', title: 'CO₂浓度', value: 0, unit: 'ppm', color: '#f5222d', icon: 'CloudOutlined' }
    ]
  }

  const latest = realTimeData.value[realTimeData.value.length - 1]
  return [
    {
      key: 'temperature',
      title: '当前温度',
      value: latest?.temperature || 0,
      unit: '°C',
      color: '#1890ff',
      icon: 'ThermometerOutlined',
      precision: 1,
      status: getTemperatureStatus(latest?.temperature),
      statusColor: getTemperatureStatusColor(latest?.temperature)
    },
    {
      key: 'humidity',
      title: '当前湿度',
      value: latest?.humidity || 0,
      unit: '%',
      color: '#52c41a',
      icon: 'EyeOutlined',
      precision: 1,
      status: getHumidityStatus(latest?.humidity),
      statusColor: getHumidityStatusColor(latest?.humidity)
    },
    {
      key: 'lightIntensity',
      title: '光照强度',
      value: latest?.lightIntensity || 0,
      unit: 'lux',
      color: '#faad14',
      icon: 'BulbOutlined',
      precision: 0
    },
    {
      key: 'co2Level',
      title: 'CO₂浓度',
      value: latest?.co2Level || 0,
      unit: 'ppm',
      color: '#f5222d',
      icon: 'CloudOutlined',
      precision: 0
    }
  ]
})

const temperatureChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      const param = params[0]
      return `${param.name}<br/>温度: ${param.value}°C`
    }
  },
  title: {
    text: '实时温度监控',
    left: 'center'
  },
  xAxis: {
    type: 'category',
    data: realTimeData.value.map(item =>
      dayjs(item.timestamp).format('HH:mm:ss')
    )
  },
  yAxis: {
    type: 'value',
    name: '温度 (°C)',
    min: 15,
    max: 35
  },
  series: [{
    data: realTimeData.value.map(item => item.temperature),
    type: 'line',
    smooth: true,
    name: '温度',
    lineStyle: {
      color: '#1890ff',
      width: 2
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
        ]
      }
    }
  }],
  dataZoom: [{
    type: 'inside',
    start: 0,
    end: 100
  }]
}))

const humidityChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      const param = params[0]
      return `${param.name}<br/>湿度: ${param.value}%`
    }
  },
  title: {
    text: '实时湿度监控',
    left: 'center'
  },
  xAxis: {
    type: 'category',
    data: realTimeData.value.map(item =>
      dayjs(item.timestamp).format('HH:mm:ss')
    )
  },
  yAxis: {
    type: 'value',
    name: '湿度 (%)',
    min: 40,
    max: 80
  },
  series: [{
    data: realTimeData.value.map(item => item.humidity),
    type: 'line',
    smooth: true,
    name: '湿度',
    lineStyle: {
      color: '#52c41a',
      width: 2
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
          { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
        ]
      }
    }
  }],
  dataZoom: [{
    type: 'inside',
    start: 0,
    end: 100
  }]
}))

// 方法
const connect = () => {
  if (connecting.value) return

  connecting.value = true

  websocketManager.connect({
    onConnect: (frame) => {
      isConnected.value = true
      connecting.value = false
      subscribeToTopics()
      message.success('WebSocket连接成功')
    },
    onDisconnect: () => {
      isConnected.value = false
      unsubscribeFromTopics()
    },
    onError: (frame) => {
      connecting.value = false
      isConnected.value = false
      unsubscribeFromTopics()
    },
    onMessage: (message) => {
      console.log('收到消息:', message)
    }
  })
}

const subscribeToTopics = () => {
  // 订阅环境数据
  environmentSubscription = websocketManager.subscribe(
    `/topic/environment/${props.areaId}`,
    (data) => {
      if (data.type === 'environment_update') {
        handleEnvironmentUpdate(data.data)
      }
    }
  )

  // 订阅告警信息
  alertSubscription = websocketManager.subscribe(
    `/topic/environment/${props.areaId}/alerts`,
    (data) => {
      if (data.type === 'environment_alert') {
        handleAlert(data)
      }
    }
  )
}

const unsubscribeFromTopics = () => {
  if (environmentSubscription) {
    websocketManager.unsubscribe(`/topic/environment/${props.areaId}`)
    environmentSubscription = null
  }

  if (alertSubscription) {
    websocketManager.unsubscribe(`/topic/environment/${props.areaId}/alerts`)
    alertSubscription = null
  }
}

const handleEnvironmentUpdate = (data) => {
  // 添加时间戳
  data.timestamp = new Date().toISOString()

  // 更新实时数据
  realTimeData.value.push(data)

  // 保持最近100条数据
  if (realTimeData.value.length > 100) {
    realTimeData.value.shift()
  }
}

const handleAlert = (alert) => {
  alerts.value.unshift(alert)

  // 保持最近20条告警
  if (alerts.value.length > 20) {
    alerts.value.pop()
  }

  // 显示通知
  if (alert.severity === 'error' || alert.severity === 'critical') {
    message.error(alert.message)
  } else {
    message.warning(alert.message)
  }
}

const toggleDevice = (deviceId, status) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (!device) return

  device.loading = true

  // 发送控制命令
  websocketManager.send(
    `/app/environment/${props.areaId}/command`,
    {
      deviceId,
      action: status ? 'turn_on' : 'turn_off',
      timestamp: new Date().toISOString()
    }
  )

  // 模拟设备响应
  setTimeout(() => {
    device.loading = false
    message.success(`${device.name} 已${status ? '开启' : '关闭'}`)
  }, 1000)
}

const adjustDevice = (deviceId, params) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (!device) return

  device.loading = true

  // 发送调整参数命令
  websocketManager.send(
    `/app/environment/${props.areaId}/command`,
    {
      deviceId,
      action: 'adjust',
      params,
      timestamp: new Date().toISOString()
    }
  )

  // 模拟设备响应
  setTimeout(() => {
    device.loading = false
    message.success(`${device.name} 参数已调整`)
  }, 1000)
}

const getTemperatureStatus = (temperature) => {
  if (!temperature) return '正常'
  if (temperature < 18) return '过低'
  if (temperature > 28) return '过高'
  return '正常'
}

const getTemperatureStatusColor = (temperature) => {
  if (!temperature) return 'green'
  if (temperature < 18) return 'blue'
  if (temperature > 28) return 'red'
  return 'green'
}

const getHumidityStatus = (humidity) => {
  if (!humidity) return '正常'
  if (humidity < 50) return '过低'
  if (humidity > 70) return '过高'
  return '正常'
}

const getHumidityStatusColor = (humidity) => {
  if (!humidity) return 'green'
  if (humidity < 50) return 'blue'
  if (humidity > 70) return 'orange'
  return 'green'
}

const getAlertColor = (severity) => {
  const colors = {
    info: 'blue',
    warning: 'orange',
    error: 'red',
    critical: 'red'
  }
  return colors[severity] || 'default'
}

const formatTime = (timestamp) => {
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  connect()
})

onUnmounted(() => {
  unsubscribeFromTopics()
  websocketManager.disconnect()
})

// 监听区域ID变化
watch(() => props.areaId, (newAreaId) => {
  if (isConnected.value) {
    unsubscribeFromTopics()
    subscribeToTopics()
  }
})
</script>

<style scoped>
.realtime-monitor {
  padding: 16px;
}

.connection-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.metric-status {
  margin-top: 8px;
}

.device-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.text-gray-500 {
  color: #6b7280;
}
</style>