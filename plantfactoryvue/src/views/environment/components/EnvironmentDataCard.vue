<template>
  <a-card :class="['data-card', `status-${status}`]" :bordered="false">
    <div class="card-content">
      <div class="card-icon">
        <component :is="getIcon()" />
      </div>
      <div class="card-info">
        <div class="card-title">{{ title }}</div>
        <div class="card-value">
          {{ value?.toFixed(1) || 0 }}
          <span class="unit">{{ unit }}</span>
        </div>
        <div class="card-trend" v-if="trend && trend.length > 1">
          <TrendIndicator :data="trend" />
        </div>
      </div>
      <div class="card-status">
        <a-badge :status="getStatusBadge()" :text="getStatusText()" />
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { computed } from 'vue'
import {
  ThermometerOutlined,
  DashboardOutlined,
  BulbOutlined,
  CloudOutlined
} from '@ant-design/icons-vue'
import TrendIndicator from './TrendIndicator.vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: Number,
    default: 0
  },
  unit: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'normal', // normal, high, low
    validator: (value) => ['normal', 'high', 'low'].includes(value)
  },
  icon: {
    type: String,
    default: 'thermometer' // thermometer, water, sun, cloud
  },
  trend: {
    type: Array,
    default: () => []
  }
})

const iconMap = {
  thermometer: ThermometerOutlined,
  water: DashboardOutlined,
  sun: BulbOutlined,
  cloud: CloudOutlined
}

const getIcon = () => {
  return iconMap[props.icon] || ThermometerOutlined
}

const getStatusBadge = () => {
  const statusMap = {
    normal: 'success',
    high: 'error',
    low: 'warning'
  }
  return statusMap[props.status] || 'default'
}

const getStatusText = () => {
  const statusTextMap = {
    normal: '正常',
    high: '偏高',
    low: '偏低'
  }
  return statusTextMap[props.status] || '未知'
}
</script>

<style scoped lang="less">
.data-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }

  &.status-normal {
    border-left: 4px solid #52c41a;
  }

  &.status-high {
    border-left: 4px solid #ff4d4f;
  }

  &.status-low {
    border-left: 4px solid #faad14;
  }

  :deep(.ant-card-body) {
    padding: 20px;
  }

  .card-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .card-icon {
    font-size: 32px;
    color: #1890ff;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60px;
    height: 60px;
    background: rgba(24, 144, 255, 0.1);
    border-radius: 50%;
    flex-shrink: 0;
  }

  .card-info {
    flex: 1;
    min-width: 0;

    .card-title {
      font-size: 14px;
      color: #8c8c8c;
      margin-bottom: 4px;
      font-weight: 500;
    }

    .card-value {
      font-size: 28px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 8px;
      display: flex;
      align-items: baseline;
      gap: 4px;

      .unit {
        font-size: 16px;
        font-weight: normal;
        color: #8c8c8c;
      }
    }

    .card-trend {
      font-size: 12px;
    }
  }

  .card-status {
    text-align: right;
    flex-shrink: 0;

    :deep(.ant-badge-status-text) {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .data-card {
    .card-content {
      flex-direction: column;
      text-align: center;
      gap: 12px;
    }

    .card-icon {
      margin: 0 auto;
    }

    .card-status {
      text-align: center;
    }

    .card-value {
      font-size: 24px !important;
    }
  }
}
</style>