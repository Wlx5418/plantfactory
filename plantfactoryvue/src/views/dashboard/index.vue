<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <a-row :gutter="24" class="dashboard-cards">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card">
          <a-statistic
            title="生产区域总数"
            :value="statistics.totalAreas"
            :prefix="h(HomeOutlined)"
            :value-style="{ color: '#3f8600' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card">
          <a-statistic
            title="环境监测点"
            :value="statistics.monitoringPoints"
            :prefix="h(EyeOutlined)"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card">
          <a-statistic
            title="今日数据记录"
            :value="statistics.todayRecords"
            :prefix="h(DatabaseOutlined)"
            :value-style="{ color: '#722ed1' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card">
          <a-statistic
            title="系统告警数"
            :value="statistics.alertCount"
            :prefix="h(ExclamationCircleOutlined)"
            :value-style="{ color: '#cf1322' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="24" class="dashboard-charts">
      <!-- 环境数据趋势图 -->
      <a-col :xs="24" :lg="16">
        <a-card title="环境数据趋势" :bordered="false">
          <div class="chart-placeholder" style="height: 400px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 6px;">
            <p>环境数据图表 (ECharts组件暂时禁用)</p>
          </div>
        </a-card>
      </a-col>

      <!-- 区域使用率 -->
      <a-col :xs="24" :lg="8">
        <a-card title="区域使用率" :bordered="false">
          <div class="chart-placeholder" style="height: 400px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 6px;">
            <p>区域使用率图表 (ECharts组件暂时禁用)</p>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 实时数据表格 -->
    <a-row :gutter="24" class="dashboard-table">
      <a-col :span="24">
        <a-card title="最新环境数据" :bordered="false">
          <a-table
            :columns="tableColumns"
            :data-source="latestData"
            :loading="tableLoading"
            :pagination="false"
            size="middle"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ record.statusText }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" size="small" @click="viewDetail(record)">
                  查看详情
                </a-button>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 快捷操作 -->
    <a-row :gutter="24" class="dashboard-actions">
      <a-col :span="24">
        <a-card title="快捷操作" :bordered="false">
          <div class="action-buttons">
            <a-space wrap size="large">
              <a-button type="primary" :icon="h(PlusOutlined)" @click="addEnvironmentData">
                记录环境数据
              </a-button>
              <a-button :icon="h(FileExcelOutlined)" @click="exportData">
                导出数据
              </a-button>
              <a-button :icon="h(SettingOutlined)" @click="systemSettings">
                系统设置
              </a-button>
              <a-button :icon="h(FileTextOutlined)" @click="viewReports">
                查看报表
              </a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
// import VChart from 'vue-echarts'
// import { use } from 'echarts/core'
// import {
//   CanvasRenderer
// } from 'echarts/renderers'
// import {
//   LineChart,
//   PieChart
// } from 'echarts/charts'
// import {
//   TitleComponent,
//   TooltipComponent,
//   LegendComponent,
//   GridComponent
// } from 'echarts/components'
import {
  HomeOutlined,
  EyeOutlined,
  DatabaseOutlined,
  ExclamationCircleOutlined,
  PlusOutlined,
  FileExcelOutlined,
  SettingOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'

// 注册ECharts组件
// use([
//   CanvasRenderer,
//   LineChart,
//   PieChart,
//   TitleComponent,
//   TooltipComponent,
//   LegendComponent,
//   GridComponent
// ])

const router = useRouter()

// 工具函数
const generateTimeLabels = (hours) => {
  const labels = []
  const now = new Date()
  for (let i = hours - 1; i >= 0; i--) {
    const time = new Date(now - i * 60 * 60 * 1000)
    labels.push(time.getHours() + ':00')
  }
  return labels
}

const generateRandomData = (count, min, max) => {
  const data = []
  for (let i = 0; i < count; i++) {
    data.push(Math.floor(Math.random() * (max - min + 1)) + min)
  }
  return data
}

const getStatusColor = (status) => {
  const colors = {
    '正常': 'green',
    '告警': 'orange',
    '异常': 'red'
  }
  return colors[status] || 'default'
}

// 响应式数据
const statistics = reactive({
  totalAreas: 12,
  monitoringPoints: 45,
  todayRecords: 238,
  alertCount: 3
})

const latestData = ref([])
const tableLoading = ref(false)

// 表格列配置
const tableColumns = [
  {
    title: '区域名称',
    dataIndex: 'areaName',
    key: 'areaName'
  },
  {
    title: '温度 (°C)',
    dataIndex: 'temperature',
    key: 'temperature',
    render: (value) => `${value}°C`
  },
  {
    title: '湿度 (%)',
    dataIndex: 'humidity',
    key: 'humidity',
    render: (value) => `${value}%`
  },
  {
    title: '光照 (lux)',
    dataIndex: 'lightIntensity',
    key: 'lightIntensity'
  },
  {
    title: 'CO2 (ppm)',
    dataIndex: 'co2Level',
    key: 'co2Level'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status'
  },
  {
    title: '更新时间',
    dataIndex: 'dataTime',
    key: 'dataTime'
  },
  {
    title: '操作',
    key: 'action',
    width: 100
  }
]

// 环境数据趋势图配置
const environmentChartOption = ref({
  title: {
    text: '24小时环境参数变化'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['温度', '湿度', '光照']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: generateTimeLabels(24)
  },
  yAxis: [
    {
      type: 'value',
      name: '温度(°C)',
      position: 'left',
      axisLine: {
        lineStyle: {
          color: '#5470c6'
        }
      }
    },
    {
      type: 'value',
      name: '湿度(%)',
      position: 'right',
      axisLine: {
        lineStyle: {
          color: '#91cc75'
        }
      }
    }
  ],
  series: [
    {
      name: '温度',
      type: 'line',
      smooth: true,
      data: generateRandomData(24, 20, 30),
      itemStyle: {
        color: '#ff7300'
      }
    },
    {
      name: '湿度',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: generateRandomData(24, 60, 80),
      itemStyle: {
        color: '#37a2da'
      }
    },
    {
      name: '光照',
      type: 'line',
      smooth: true,
      data: generateRandomData(24, 1000, 8000),
      itemStyle: {
        color: '#ffd700'
      }
    }
  ]
})

// 区域使用率饼图配置
const usageChartOption = ref({
  title: {
    text: '生产区域使用情况',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '使用情况',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '40',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 8, name: '使用中', itemStyle: { color: '#52c41a' } },
        { value: 3, name: '空闲', itemStyle: { color: '#1890ff' } },
        { value: 1, name: '维护中', itemStyle: { color: '#faad14' } }
      ]
    }
  ]
})


const fetchDashboardData = async () => {
  tableLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 生成模拟数据
    latestData.value = [
      {
        key: '1',
        areaName: '生长区A',
        temperature: 25.5,
        humidity: 75,
        lightIntensity: 5500,
        co2Level: 800,
        status: '正常',
        statusText: '正常',
        dataTime: '2024-01-11 14:30:00'
      },
      {
        key: '2',
        areaName: '生长区B',
        temperature: 28.2,
        humidity: 65,
        lightIntensity: 6200,
        co2Level: 750,
        status: '告警',
        statusText: '温度偏高',
        dataTime: '2024-01-11 14:29:00'
      },
      {
        key: '3',
        areaName: '育苗区',
        temperature: 24.8,
        humidity: 80,
        lightIntensity: 4800,
        co2Level: 850,
        status: '正常',
        statusText: '正常',
        dataTime: '2024-01-11 14:28:00'
      }
    ]
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    tableLoading.value = false
  }
}

const viewDetail = (record) => {
  // 跳转到环境监控页面
  router.push({
    path: '/production/monitoring',
    query: { areaId: record.areaId }
  })
}

const addEnvironmentData = () => {
  router.push('/production/monitoring')
}

const exportData = () => {
  // 导出数据功能
  console.log('导出数据')
}

const systemSettings = () => {
  router.push('/system/settings')
}

const viewReports = () => {
  router.push('/data/analysis')
}

// 生命周期
onMounted(() => {
  fetchDashboardData()

  // 定时刷新数据
  const timer = setInterval(fetchDashboardData, 30000)

  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(timer)
  })
})
</script>

<style scoped lang="less">
.dashboard {
  .dashboard-cards {
    margin-bottom: 24px;

    .stat-card {
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border-radius: 8px;

      :deep(.ant-card-body) {
        padding: 24px;
      }

      :deep(.ant-statistic-content) {
        font-size: 24px;
      }
    }
  }

  .dashboard-charts {
    margin-bottom: 24px;

    :deep(.ant-card) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border-radius: 8px;
    }
  }

  .dashboard-table {
    margin-bottom: 24px;

    :deep(.ant-card) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border-radius: 8px;
    }
  }

  .dashboard-actions {
    :deep(.ant-card) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border-radius: 8px;
    }

    .action-buttons {
      text-align: center;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    .dashboard-cards {
      .stat-card {
        margin-bottom: 16px;
      }
    }
  }
}
</style>