<template>
  <div class="historical-data-page">
    <page-header title="历史数据" subtitle="查看环境数据的历史记录和趋势分析" />

    <div class="content-container">
      <!-- 查询条件 -->
      <a-card class="search-card" :bordered="false">
        <a-form
          :model="searchForm"
          layout="inline"
          @finish="handleSearch"
        >
          <a-form-item label="数据类型">
            <a-select
              v-model:value="searchForm.dataType"
              placeholder="选择数据类型"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="temperature">温度</a-select-option>
              <a-select-option value="humidity">湿度</a-select-option>
              <a-select-option value="light">光照强度</a-select-option>
              <a-select-option value="co2">CO2浓度</a-select-option>
              <a-select-option value="ph">pH值</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
            />
          </a-form-item>

          <a-form-item label="分区">
            <a-select
              v-model:value="searchForm.areaId"
              placeholder="选择分区"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="1">A区</a-select-option>
              <a-select-option value="2">B区</a-select-option>
              <a-select-option value="3">C区</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="loading">
                <search-outlined />
                查询
              </a-button>
              <a-button @click="handleReset">重置</a-button>
              <a-button type="dashed" @click="handleExport">
                <export-outlined />
                导出数据
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 数据趋势图表 -->
      <a-card title="数据趋势图" class="chart-card" :bordered="false">
        <div class="chart-container">
          <v-chart
            class="chart"
            :option="chartOption"
            :loading="chartLoading"
            autoresize
          />
        </div>
      </a-card>

      <!-- 数据表格 -->
      <a-card title="历史数据记录" class="table-card" :bordered="false">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
          :scroll="{ x: 1200 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="viewDetails(record)">
                  详情
                </a-button>
                <a-button type="link" size="small" @click="compareData(record)">
                  对比
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ExportOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  CanvasRenderer
])

// 查询表单
const searchForm = reactive({
  dataType: undefined,
  dateRange: [],
  areaId: undefined
})

// 数据状态
const loading = ref(false)
const chartLoading = ref(false)
const dataSource = ref([])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 图表配置
const chartOption = computed(() => ({
  title: {
    text: '环境数据趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['数值', '预警线'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
  },
  yAxis: {
    type: 'value'
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100
    }
  ],
  series: [
    {
      name: '数值',
      type: 'line',
      smooth: true,
      data: [22, 23, 25, 28, 26, 24, 23],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '预警线',
      type: 'line',
      smooth: true,
      data: [30, 30, 30, 30, 30, 30, 30],
      itemStyle: {
        color: '#ff4d4f'
      },
      lineStyle: {
        type: 'dashed'
      }
    }
  ]
}))

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'dataType',
    width: 100
  },
  {
    title: '数值',
    dataIndex: 'value',
    key: 'value',
    width: 100
  },
  {
    title: '单位',
    dataIndex: 'unit',
    key: 'unit',
    width: 80
  },
  {
    title: '分区',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 100
  },
  {
    title: '记录时间',
    dataIndex: 'recordTime',
    key: 'recordTime',
    width: 180
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 120
  }
]

// 生成模拟数据
const generateMockData = () => {
  const data = []
  const dataTypes = ['temperature', 'humidity', 'light', 'co2', 'ph']
  const areas = ['A区', 'B区', 'C区']

  for (let i = 1; i <= 100; i++) {
    const dataType = dataTypes[Math.floor(Math.random() * dataTypes.length)]
    let value, unit, status

    switch (dataType) {
      case 'temperature':
        value = (20 + Math.random() * 15).toFixed(1)
        unit = '°C'
        status = parseFloat(value) > 30 ? 'warning' : 'normal'
        break
      case 'humidity':
        value = (40 + Math.random() * 40).toFixed(1)
        unit = '%'
        status = parseFloat(value) < 50 ? 'warning' : 'normal'
        break
      case 'light':
        value = Math.floor(1000 + Math.random() * 5000)
        unit = 'lux'
        status = 'normal'
        break
      case 'co2':
        value = Math.floor(300 + Math.random() * 200)
        unit = 'ppm'
        status = 'normal'
        break
      case 'ph':
        value = (5.5 + Math.random() * 2).toFixed(1)
        unit = 'pH'
        status = 'normal'
        break
    }

    data.push({
      id: i,
      dataType: dataType === 'temperature' ? '温度' :
                dataType === 'humidity' ? '湿度' :
                dataType === 'light' ? '光照' :
                dataType === 'co2' ? 'CO2' : 'pH',
      value,
      unit,
      areaName: areas[Math.floor(Math.random() * areas.length)],
      recordTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString(),
      status
    })
  }

  return data
}

// 搜索处理
const handleSearch = () => {
  loading.value = true
  pagination.current = 1

  setTimeout(() => {
    dataSource.value = generateMockData()
    pagination.total = 100
    loading.value = false
    message.success('查询完成')
  }, 1000)
}

// 重置查询
const handleReset = () => {
  Object.assign(searchForm, {
    dataType: undefined,
    dateRange: [],
    areaId: undefined
  })
  handleSearch()
}

// 导出数据
const handleExport = () => {
  message.info('导出功能开发中...')
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  handleSearch()
}

// 查看详情
const viewDetails = (record) => {
  message.info(`查看记录 ${record.id} 的详情`)
}

// 对比数据
const compareData = (record) => {
  message.info(`对比记录 ${record.id} 的数据`)
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    normal: 'green',
    warning: 'orange',
    error: 'red'
  }
  return colors[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    normal: '正常',
    warning: '预警',
    error: '异常'
  }
  return texts[status] || '未知'
}

// 组件挂载
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.historical-data-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart {
  height: 100%;
  width: 100%;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }

  .chart-container {
    height: 300px;
  }
}
</style>