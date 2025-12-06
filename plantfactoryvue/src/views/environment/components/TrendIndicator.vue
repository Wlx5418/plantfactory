<template>
  <div :class="['trend-indicator', trendClass]">
    <component :is="trendIcon" />
    <span class="trend-value">{{ trendValue }}%</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
  data: {
    type: Array,
    required: true,
    validator: (value) => value.length >= 2
  }
})

const trendValue = computed(() => {
  if (props.data.length < 2) return 0
  const current = props.data[props.data.length - 1]
  const previous = props.data[props.data.length - 2]
  if (previous === 0) return 0
  return ((current - previous) / previous * 100).toFixed(1)
})

const trendDirection = computed(() => {
  const value = parseFloat(trendValue.value)
  if (Math.abs(value) < 0.1) return 'stable'
  return value > 0 ? 'up' : 'down'
})

const trendClass = computed(() => {
  const direction = trendDirection.value
  return `trend-${direction}`
})

const trendIcon = computed(() => {
  const direction = trendDirection.value
  switch (direction) {
    case 'up':
      return ArrowUpOutlined
    case 'down':
      return ArrowDownOutlined
    default:
      return MinusOutlined
  }
})
</script>

<style scoped lang="less">
.trend-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;

  &.trend-up {
    color: #ff4d4f;

    svg {
      transform: rotate(0deg);
    }
  }

  &.trend-down {
    color: #52c41a;

    svg {
      transform: rotate(0deg);
    }
  }

  &.trend-stable {
    color: #8c8c8c;
  }

  .trend-value {
    font-weight: 500;
  }
}
</style>