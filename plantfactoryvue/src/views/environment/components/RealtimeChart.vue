<template>
  <div class="realtime-chart">
    <div v-if="loading" class="chart-loading">
      <a-spin size="large" />
    </div>
    <div v-else-if="!data || data.length === 0" class="chart-empty">
      <a-empty description="暂无数据" />
    </div>
    <div v-else ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  timeRange: {
    type: String,
    default: '1h'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref(null)
let chartInstance = null

const getTimeLabels = (range) => {
  const labels = []
  const now = new Date()
  let interval = 5 // 分钟
  let count = 12

  switch (range) {
    case '1h':
      interval = 5
      count = 12
      break
    case '6h':
      interval = 30
      count = 12
      break
    case '24h':
      interval = 120 // 2小时
      count = 12
      break
    case '7d':
      interval = 1440 // 24小时
      count = 7
      break
    default:
      interval = 5
      count = 12
  }

  for (let i = count - 1; i >= 0; i--) {
    const time = new Date(now - i * interval * 60 * 1000)
    if (range === '7d') {
      labels.push(time.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }))
    } else {
      labels.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
    }
  }

  return labels
}

const generateMockData = (count, min, max) => {
  const data = []
  for (let i = 0; i < count; i++) {
    data.push(Math.random() * (max - min) + min)
  }
  return data
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  const timeLabels = getTimeLabels(props.timeRange)
  const dataCount = timeLabels.length

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: function (params) {
        let result = `${params[0].axisValue}<br/>`
        params.forEach(param => {
          result += `${param.marker} ${param.seriesName}: ${param.value.toFixed(1)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['温度', '湿度', '光照', 'CO₂'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 40,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timeLabels,
      axisLabel: {
        rotate: props.timeRange === '7d' ? 0 : 45
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '温度(°C)',
        position: 'left',
        min: 15,
        max: 35,
        axisLine: {
          lineStyle: {
            color: '#ff7300'
          }
        }
      },
      {
        type: 'value',
        name: '湿度(%)',
        position: 'right',
        min: 40,
        max: 100,
        axisLine: {
          lineStyle: {
            color: '#37a2da'
          }
        }
      }
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        smooth: true,
        data: generateMockData(dataCount, 22, 28),
        itemStyle: {
          color: '#ff7300'
        },
        lineStyle: {
          width: 2
        }
      },
      {
        name: '湿度',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: generateMockData(dataCount, 60, 80),
        itemStyle: {
          color: '#37a2da'
        },
        lineStyle: {
          width: 2
        }
      },
      {
        name: '光照',
        type: 'line',
        smooth: true,
        data: generateMockData(dataCount, 3000, 8000),
        itemStyle: {
          color: '#ffd700'
        },
        lineStyle: {
          width: 2
        }
      },
      {
        name: 'CO₂',
        type: 'line',
        smooth: true,
        data: generateMockData(dataCount, 600, 1200),
        itemStyle: {
          color: '#67c23a'
        },
        lineStyle: {
          width: 2
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => props.timeRange, () => {
  nextTick(() => {
    updateChart()
  })
})

watch(() => props.loading, (newLoading) => {
  if (!newLoading) {
    nextTick(() => {
      if (chartInstance) {
        updateChart()
      } else {
        initChart()
      }
    })
  }
})

onMounted(() => {
  nextTick(() => {
    if (!props.loading) {
      initChart()
    }
  })
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', resizeChart)
})
</script>

<style scoped lang="less">
.realtime-chart {
  width: 100%;
  height: 100%;
  position: relative;

  .chart-loading,
  .chart-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 200px;
  }

  .chart-container {
    width: 100%;
    height: 100%;
    min-height: 200px;
  }
}
</style>