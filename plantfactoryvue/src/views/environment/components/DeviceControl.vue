<template>
  <div class="device-control">
    <div v-if="!devices || devices.length === 0" class="device-empty">
      <a-empty description="暂无设备" />
    </div>
    <div v-else class="device-list">
      <div
        v-for="device in devices"
        :key="device.id"
        :class="['device-item', { 'device-offline': device.status === 'offline' }]"
      >
        <div class="device-header">
          <div class="device-info">
            <div class="device-icon">
              <component :is="getDeviceIcon(device.type)" />
            </div>
            <div class="device-details">
              <div class="device-name">{{ device.name }}</div>
              <div class="device-type">{{ getDeviceTypeText(device.type) }}</div>
            </div>
          </div>
          <div class="device-status">
            <a-badge :status="getStatusBadge(device.status)" :text="getStatusText(device.status)" />
          </div>
        </div>

        <div class="device-controls">
          <div class="control-buttons">
            <a-button
              :type="device.status === 'on' || device.status === 'running' ? 'default' : 'primary'"
              size="small"
              @click="toggleDevice(device)"
              :disabled="device.status === 'offline'"
            >
              {{ device.status === 'on' || device.status === 'running' ? '关闭' : '开启' }}
            </a-button>
            <a-button
              type="dashed"
              size="small"
              @click="toggleMode(device)"
              :disabled="device.status === 'offline'"
            >
              {{ device.autoMode ? '手动' : '自动' }}
            </a-button>
          </div>

          <!-- 设备特定设置 -->
          <div class="device-settings" v-if="device.status !== 'offline'">
            <template v-if="device.type === 'fan'">
              <div class="setting-item">
                <span class="setting-label">风速:</span>
                <a-slider
                  v-model:value="device.settings.speed"
                  :min="0"
                  :max="100"
                  :disabled="device.autoMode"
                  @change="updateDeviceSetting(device, 'speed', $event)"
                />
              </div>
            </template>

            <template v-if="device.type === 'light'">
              <div class="setting-item">
                <span class="setting-label">亮度:</span>
                <a-slider
                  v-model:value="device.settings.brightness"
                  :min="0"
                  :max="100"
                  :disabled="device.autoMode"
                  @change="updateDeviceSetting(device, 'brightness', $event)"
                />
              </div>
            </template>

            <template v-if="device.type === 'pump'">
              <div class="setting-item">
                <span class="setting-label">流量:</span>
                <a-slider
                  v-model:value="device.settings.flowRate"
                  :min="0"
                  :max="100"
                  :disabled="device.autoMode"
                  @change="updateDeviceSetting(device, 'flowRate', $event)"
                />
              </div>
            </template>
          </div>

          <div class="device-modes" v-if="device.autoMode">
            <a-tag color="blue">自动模式</a-tag>
            <span class="mode-description">{{ getModeDescription(device) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  DashboardOutlined,
  BulbOutlined,
  CloudOutlined,
  ToolOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
  devices: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['control'])

const iconMap = {
  fan: DashboardOutlined,
  light: BulbOutlined,
  pump: CloudOutlined,
  default: ToolOutlined
}

const getDeviceIcon = (type) => {
  return iconMap[type] || iconMap.default
}

const getDeviceTypeText = (type) => {
  const typeMap = {
    fan: '通风设备',
    light: '照明设备',
    pump: '水泵设备'
  }
  return typeMap[type] || '未知设备'
}

const getStatusBadge = (status) => {
  const statusMap = {
    on: 'success',
    running: 'processing',
    off: 'default',
    offline: 'error'
  }
  return statusMap[status] || 'default'
}

const getStatusText = (status) => {
  const statusMap = {
    on: '开启',
    running: '运行中',
    off: '关闭',
    offline: '离线'
  }
  return statusMap[status] || '未知'
}

const getModeDescription = (device) => {
  if (device.type === 'fan') {
    return `温度达到 ${device.settings.threshold}°C 时自动开启`
  } else if (device.type === 'light') {
    return `按时间表 ${device.settings.schedule} 自动开关`
  } else if (device.type === 'pump') {
    return `每 ${device.settings.interval} 分钟自动运行`
  }
  return '自动控制中'
}

const toggleDevice = (device) => {
  const action = device.status === 'on' || device.status === 'running' ? 'off' : 'on'
  emit('control', device.id, 'toggle', { status: action })
}

const toggleMode = (device) => {
  emit('control', device.id, 'mode', { autoMode: !device.autoMode })
}

const updateDeviceSetting = (device, setting, value) => {
  emit('control', device.id, 'setting', {
    setting,
    value
  })
}
</script>

<style scoped lang="less">
.device-control {
  .device-empty {
    text-align: center;
    padding: 40px 0;
  }

  .device-list {
    .device-item {
      border: 1px solid #f0f0f0;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      background: #fafafa;
      transition: all 0.3s ease;

      &:hover {
        border-color: #d9d9d9;
        background: #fff;
      }

      &.device-offline {
        opacity: 0.6;
        background: #f5f5f5;
      }

      .device-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .device-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .device-icon {
            font-size: 24px;
            color: #1890ff;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            background: rgba(24, 144, 255, 0.1);
            border-radius: 50%;
          }

          .device-details {
            .device-name {
              font-weight: 600;
              color: #262626;
              margin-bottom: 4px;
            }

            .device-type {
              font-size: 12px;
              color: #8c8c8c;
            }
          }
        }
      }

      .device-controls {
        .control-buttons {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;
        }

        .device-settings {
          margin-bottom: 12px;

          .setting-item {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;

            .setting-label {
              font-size: 12px;
              color: #595959;
              min-width: 40px;
            }

            .ant-slider {
              flex: 1;
            }
          }
        }

        .device-modes {
          display: flex;
          align-items: center;
          gap: 8px;

          .mode-description {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .device-control {
    .device-item {
      .device-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }

      .control-buttons {
        flex-direction: column;

        button {
          width: 100%;
        }
      }
    }
  }
}
</style>