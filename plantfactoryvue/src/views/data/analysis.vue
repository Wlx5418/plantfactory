<template>
  <div class="analysis-page">
    <page-header title="数据分析" subtitle="深入分析植物工厂的环境数据和生长趋势" />

    <div class="content-container">
      <!-- 时间范围选择 -->
      <a-card class="filter-card" :bordered="false">
        <a-form
          :model="filterForm"
          layout="inline"
          @finish="handleFilter"
        >
          <a-form-item label="分析类型">
            <a-select
              v-model:value="filterForm.analysisType"
              placeholder="选择分析类型"
              style="width: 150px"
              @change="handleAnalysisTypeChange"
            >
              <a-select-option value="trend">趋势分析</a-select-option>
              <a-select-option value="correlation">关联分析</a-select-option>
              <a-select-option value="comparison">对比分析</a-select-option>
              <a-select-option value="prediction">预测分析</a-select-option>
              <a-select-option value="efficiency">效率分析</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="filterForm.dateRange"
              format="YYYY-MM-DD"
              style="width: 250px"
            />
          </a-form-item>

          <a-form-item label="分区选择">
            <a-select
              v-model:value="filterForm.areaIds"
              mode="multiple"
              placeholder="选择分区"
              style="width: 200px"
            >
              <a-select-option value="1">A区</a-select-option>
              <a-select-option value="2">B区</a-select-option>
              <a-select-option value="3">C区</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="analysisLoading">
                <bar-chart-outlined />
                开始分析
              </a-button>
              <a-button @click="handleReset">重置</a-button>
              <a-button @click="handleExportReport">
                <file-excel-outlined />
                导出报告
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 分析概览 -->
      <a-row :gutter="16" class="overview-row">
        <a-col :xs="24" :sm="6">
          <a-card class="overview-card">
            <a-statistic
              title="数据完整性"
              :value="overviewData.completeness"
              suffix="%"
              :value-style="{ color: '#1890ff' }"
            />
            <div class="trend-indicator up">
              <arrow-up-outlined />
              <span>较上期 +2.3%</span>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="6">
          <a-card class="overview-card">
            <a-statistic
              title="环境稳定性"
              :value="overviewData.stability"
              suffix="%"
              :value-style="{ color: '#52c41a' }"
            />
            <div class="trend-indicator up">
              <arrow-up-outlined />
              <span>较上期 +5.1%</span>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="6">
          <a-card class="overview-card">
            <a-statistic
              title="生长效率"
              :value="overviewData.efficiency"
              suffix="%"
              :value-style="{ color: '#faad14' }"
            />
            <div class="trend-indicator down">
              <arrow-down-outlined />
              <span>较上期 -1.2%</span>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="6">
          <a-card class="overview-card">
            <a-statistic
              title="预警次数"
              :value="overviewData.alerts"
              :value-style="{ color: '#ff4d4f' }"
            />
            <div class="trend-indicator down">
              <arrow-down-outlined />
              <span>较上期 -15次</span>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 主分析图表 -->
      <a-card title="环境数据趋势分析" class="chart-card" :bordered="false">
        <div class="chart-container">
          <v-chart
            class="chart"
            :option="mainChartOption"
            :loading="chartLoading"
            autoresize
          />
        </div>
      </a-card>

      <!-- 关联分析 -->
      <a-row :gutter="16">
        <a-col :xs="24" :lg="12">
          <a-card title="参数关联性分析" class="chart-card" :bordered="false">
            <div class="chart-container">
              <v-chart
                class="chart"
                :option="correlationChartOption"
                autoresize
              />
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="分区对比分析" class="chart-card" :bordered="false">
            <div class="chart-container">
              <v-chart
                class="chart"
                :option="comparisonChartOption"
                autoresize
              />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 预测分析 -->
      <a-card title="生长趋势预测" class="chart-card" :bordered="false">
        <div class="prediction-container">
          <a-row :gutter="16">
            <a-col :xs="24" :lg="16">
              <div class="chart-container">
                <v-chart
                  class="chart"
                  :option="predictionChartOption"
                  autoresize
                />
              </div>
            </a-col>
            <a-col :xs="24" :lg="8">
              <div class="prediction-summary">
                <h4>预测结果摘要</h4>
                <a-descriptions size="small" :column="1">
                  <a-descriptions-item label="预测周期">
                    未来7天
                  </a-descriptions-item>
                  <a-descriptions-item label="预计产量">
                    <a-statistic
                      :value="predictionData.expectedYield"
                      suffix="kg"
                      :value-style="{ color: '#52c41a' }"
                    />
                  </a-descriptions-item>
                  <a-descriptions-item label="置信度">
                    <a-progress
                      :percent="predictionData.confidence"
                      :stroke-color="getConfidenceColor(predictionData.confidence)"
                    />
                  </a-descriptions-item>
                  <a-descriptions-item label="风险等级">
                    <a-tag :color="getRiskColor(predictionData.riskLevel)">
                      {{ getRiskText(predictionData.riskLevel) }}
                    </a-tag>
                  </a-descriptions-item>
                </a-descriptions>

                <a-divider />

                <div class="prediction-actions">
                  <a-space direction="vertical" style="width: 100%">
                    <a-button type="primary" block>
                      <download-outlined />
                      下载预测报告
                    </a-button>
                    <a-button block>
                      <share-alt-outlined />
                      分享分析结果
                    </a-button>
                  </a-space>
                </div>
              </div>
            </a-col>
          </a-row>
        </div>
      </a-card>

      <!-- 详细数据表格 -->
      <a-card title="分析详细数据" class="table-card" :bordered="false">
        <a-table
          :columns="analysisColumns"
          :data-source="analysisData"
          :pagination="analysisPagination"
          row-key="id"
          @change="handleAnalysisTableChange"
          :scroll="{ x: 1200 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'indicator'">
              <a-tag :color="getIndicatorColor(record.indicator)">
                {{ getIndicatorText(record.indicator) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'changeRate'">
              <span :class="getChangeRateClass(record.changeRate)">
                {{ record.changeRate > 0 ? '+' : '' }}{{ record.changeRate }}%
                <component :is="record.changeRate > 0 ? 'ArrowUpOutlined' : 'ArrowDownOutlined'" />
              </span>
            </template>
            <template v-else-if="column.key === 'trend'">
              <a-tag :color="getTrendColor(record.trend)">
                {{ getTrendText(record.trend) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  BarChartOutlined,
  FileExcelOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  DownloadOutlined,
  ShareAltOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import {
  LineChart,
  BarChart,
  ScatterChart,
  RadarChart
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  VisualMapComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([
  LineChart,
  BarChart,
  ScatterChart,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  VisualMapComponent,
  CanvasRenderer
])

// 数据状态
const analysisLoading = ref(false)
const chartLoading = ref(false)

// 筛选表单
const filterForm = reactive({
  analysisType: 'trend',
  dateRange: [],
  areaIds: ['1', '2', '3']
})

// 概览数据
const overviewData = reactive({
  completeness: 94.5,
  stability: 87.2,
  efficiency: 76.8,
  alerts: 23
})

// 预测数据
const predictionData = reactive({
  expectedYield: 125.6,
  confidence: 85,
  riskLevel: 'low'
})

// 分析数据
const analysisData = ref([])
const analysisPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

// 主图表配置
const mainChartOption = computed(() => ({
  title: {
    text: '环境参数变化趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['温度', '湿度', '光照', 'CO2浓度'],
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
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: [
    {
      type: 'value',
      name: '温度(°C)/湿度(%)',
      position: 'left'
    },
    {
      type: 'value',
      name: '光照(lux)',
      position: 'right'
    }
  ],
  series: [
    {
      name: '温度',
      type: 'line',
      smooth: true,
      data: [23, 24, 25, 26, 25, 24, 23],
      itemStyle: { color: '#ff7300' }
    },
    {
      name: '湿度',
      type: 'line',
      smooth: true,
      data: [65, 68, 70, 72, 70, 67, 65],
      itemStyle: { color: '#ff4d4f' }
    },
    {
      name: '光照',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: [12000, 13500, 14000, 13800, 13200, 12500, 12000],
      itemStyle: { color: '#faad14' }
    },
    {
      name: 'CO2浓度',
      type: 'line',
      smooth: true,
      data: [450, 460, 465, 470, 465, 455, 450],
      itemStyle: { color: '#52c41a' }
    }
  ]
}))

// 关联分析图表
const correlationChartOption = computed(() => ({
  title: {
    text: '温度-湿度关联图',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  xAxis: {
    type: 'value',
    name: '温度(°C)',
    min: 15,
    max: 35
  },
  yAxis: {
    type: 'value',
    name: '湿度(%)',
    min: 40,
    max: 90
  },
  series: [{
    type: 'scatter',
    symbolSize: 8,
    data: Array.from({ length: 50 }, () => [
      15 + Math.random() * 20,
      40 + Math.random() * 50
    ])
  }]
}))

// 对比分析图表
const comparisonChartOption = computed(() => ({
  title: {
    text: '各分区环境对比',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['A区', 'B区', 'C区'],
    bottom: 0
  },
  radar: {
    indicator: [
      { name: '温度适宜度', max: 100 },
      { name: '湿度适宜度', max: 100 },
      { name: '光照充足度', max: 100 },
      { name: 'CO2浓度', max: 100 },
      { name: 'pH稳定性', max: 100 }
    ]
  },
  series: [{
    type: 'radar',
    data: [
      {
        value: [85, 78, 92, 88, 90],
        name: 'A区',
        itemStyle: { color: '#1890ff' }
      },
      {
        value: [78, 85, 88, 82, 86],
        name: 'B区',
        itemStyle: { color: '#52c41a' }
      },
      {
        value: [90, 82, 85, 90, 88],
        name: 'C区',
        itemStyle: { color: '#faad14' }
      }
    ]
  }]
}))

// 预测图表
const predictionChartOption = computed(() => ({
  title: {
    text: '生长趋势预测',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['历史数据', '预测数据', '置信区间'],
    bottom: 0
  },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日', '周一(下)', '周二(下)', '周三(下)']
  },
  yAxis: {
    type: 'value',
    name: '生长高度(cm)'
  },
  series: [
    {
      name: '历史数据',
      type: 'line',
      data: [10, 12, 14, 16, 18, 20, 22, null, null, null],
      itemStyle: { color: '#1890ff' }
    },
    {
      name: '预测数据',
      type: 'line',
      data: [null, null, null, null, null, null, 22, 24, 26, 28],
      itemStyle: { color: '#52c41a' },
      lineStyle: { type: 'dashed' }
    },
    {
      name: '置信区间',
      type: 'line',
      data: [null, null, null, null, null, null, [21, 23], [23, 25], [25, 27], [27, 29]],
      itemStyle: { color: '#faad14' },
      areaStyle: { opacity: 0.3 }
    }
  ]
}))

// 分析表格列配置
const analysisColumns = [
  {
    title: '指标名称',
    dataIndex: 'indicator',
    key: 'indicator',
    width: 120
  },
  {
    title: '当前值',
    dataIndex: 'currentValue',
    key: 'currentValue',
    width: 100
  },
  {
    title: '基准值',
    dataIndex: 'baselineValue',
    key: 'baselineValue',
    width: 100
  },
  {
    title: '变化率',
    key: 'changeRate',
    width: 100
  },
  {
    title: '趋势',
    key: 'trend',
    width: 100
  },
  {
    title: '建议',
    dataIndex: 'recommendation',
    key: 'recommendation',
    ellipsis: true
  }
]

// 生成分析数据
const generateAnalysisData = () => {
  const indicators = ['temperature', 'humidity', 'light', 'co2', 'ph']
  const trends = ['上升', '下降', '稳定']

  return indicators.map((indicator, index) => ({
    id: index + 1,
    indicator,
    currentValue: (20 + Math.random() * 20).toFixed(1),
    baselineValue: (20 + Math.random() * 20).toFixed(1),
    changeRate: (Math.random() * 20 - 10).toFixed(1),
    trend: trends[Math.floor(Math.random() * trends.length)],
    recommendation: `${getIndicatorText(indicator)}${Math.random() > 0.5 ? '正常，继续保持' : '需要调整'}`
  }))
}

// 获取指标颜色
const getIndicatorColor = (indicator) => {
  const colors = {
    temperature: 'red',
    humidity: 'blue',
    light: 'yellow',
    co2: 'green',
    ph: 'purple'
  }
  return colors[indicator] || 'default'
}

// 获取指标文本
const getIndicatorText = (indicator) => {
  const texts = {
    temperature: '温度',
    humidity: '湿度',
    light: '光照',
    co2: 'CO2浓度',
    ph: 'pH值'
  }
  return texts[indicator] || indicator
}

// 获取变化率样式
const getChangeRateClass = (rate) => {
  return rate > 0 ? 'positive-change' : 'negative-change'
}

// 获取趋势颜色
const getTrendColor = (trend) => {
  const colors = {
    '上升': 'red',
    '下降': 'green',
    '稳定': 'blue'
  }
  return colors[trend] || 'default'
}

// 获取趋势文本
const getTrendText = (trend) => {
  return trend
}

// 获取置信度颜色
const getConfidenceColor = (confidence) => {
  if (confidence >= 80) return '#52c41a'
  if (confidence >= 60) return '#faad14'
  return '#ff4d4f'
}

// 获取风险颜色
const getRiskColor = (risk) => {
  const colors = {
    low: 'green',
    medium: 'orange',
    high: 'red'
  }
  return colors[risk] || 'default'
}

// 获取风险文本
const getRiskText = (risk) => {
  const texts = {
    low: '低风险',
    medium: '中风险',
    high: '高风险'
  }
  return texts[risk] || '未知'
}

// 处理分析类型变化
const handleAnalysisTypeChange = (value) => {
  console.log('分析类型变化:', value)
}

// 处理筛选
const handleFilter = () => {
  analysisLoading.value = true
  chartLoading.value = true

  setTimeout(() => {
    analysisData.value = generateAnalysisData()
    analysisPagination.total = analysisData.value.length
    analysisLoading.value = false
    chartLoading.value = false
    message.success('分析完成')
  }, 2000)
}

// 重置筛选
const handleReset = () => {
  Object.assign(filterForm, {
    analysisType: 'trend',
    dateRange: [],
    areaIds: ['1', '2', '3']
  })
  handleFilter()
}

// 导出报告
const handleExportReport = () => {
  message.info('正在生成分析报告...')
}

// 分析表格变化处理
const handleAnalysisTableChange = (pagination) => {
  analysisPagination.current = pagination.current
  analysisPagination.pageSize = pagination.pageSize
}

// 组件挂载
onMounted(() => {
  handleFilter()
})
</script>

<style scoped>
.analysis-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.overview-row {
  margin-bottom: 16px;
}

.overview-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.trend-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 8px;
}

.trend-indicator.up {
  color: #52c41a;
}

.trend-indicator.down {
  color: #ff4d4f;
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

.prediction-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prediction-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prediction-actions {
  margin-top: 16px;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.positive-change {
  color: #52c41a;
}

.negative-change {
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }

  .chart-container {
    height: 300px;
  }

  .overview-row .ant-col {
    margin-bottom: 8px;
  }
}
</style>