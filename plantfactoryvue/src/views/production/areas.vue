<template>
  <div class="areas-page">
    <page-header title="分区管理" subtitle="管理植物工厂的生产分区和区域配置" />

    <div class="content-container">
      <!-- 操作区域 -->
      <a-card class="action-card" :bordered="false">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <plus-outlined />
            新建分区
          </a-button>
          <a-button @click="handleRefresh">
            <reload-outlined />
            刷新
          </a-button>
        </a-space>
      </a-card>

      <!-- 分区概览 -->
      <a-row :gutter="16" class="stats-row">
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="总分区数"
              :value="stats.totalAreas"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="运行中"
              :value="stats.activeAreas"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="维护中"
              :value="stats.maintenanceAreas"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card class="stat-card">
            <a-statistic
              title="总容量"
              :value="stats.totalCapacity"
              suffix="m²"
              :value-style="{ color: '#722ed1' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 分区卡片列表 -->
      <div class="areas-grid">
        <a-card
          v-for="area in areas"
          :key="area.id"
          class="area-card"
          :hoverable="true"
          :class="{ 'area-offline': !area.online }"
        >
          <template #title>
            <div class="area-header">
              <div class="area-info">
                <h3>{{ area.name }}</h3>
                <a-tag :color="area.online ? 'green' : 'red'">
                  {{ area.online ? '在线' : '离线' }}
                </a-tag>
              </div>
              <div class="area-actions">
                <a-dropdown>
                  <a-button type="text" size="small">
                    <more-outlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="editArea(area)">
                        <edit-outlined /> 编辑
                      </a-menu-item>
                      <a-menu-item @click="viewAreaDetails(area)">
                        <eye-outlined /> 详情
                      </a-menu-item>
                      <a-menu-item @click="controlArea(area)">
                        <control-outlined /> 控制
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item @click="deleteArea(area)" danger>
                        <delete-outlined /> 删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </div>
          </template>

          <div class="area-content">
            <!-- 环境数据 -->
            <div class="environment-data">
              <div class="data-row">
                <div class="data-item">
                  <div class="data-label">温度</div>
                  <div class="data-value">
                    <thermometer-outlined />
                    {{ area.temperature }}°C
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">湿度</div>
                  <div class="data-value">
                    <drop-outlined />
                    {{ area.humidity }}%
                  </div>
                </div>
              </div>
              <div class="data-row">
                <div class="data-item">
                  <div class="data-label">光照</div>
                  <div class="data-value">
                    <bulb-outlined />
                    {{ area.light }}lux
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">CO₂</div>
                  <div class="data-value">
                    <cloud-outlined />
                    {{ area.co2 }}ppm
                  </div>
                </div>
              </div>
            </div>

            <!-- 设备状态 -->
            <div class="device-status">
              <h4>设备状态</h4>
              <div class="devices-grid">
                <div
                  v-for="device in area.devices"
                  :key="device.id"
                  class="device-item"
                  :class="{ 'device-offline': !device.online }"
                >
                  <component :is="getDeviceIcon(device.type)" />
                  <span class="device-name">{{ device.name }}</span>
                  <a-switch
                    :checked="device.running"
                    size="small"
                    @change="(checked) => toggleDevice(device.id, checked)"
                  />
                </div>
              </div>
            </div>

            <!-- 当前作物 -->
            <div class="crop-info">
              <h4>当前作物</h4>
              <div class="crop-details">
                <a-avatar :src="area.crop.image" :size="40">
                  {{ area.crop.name.charAt(0) }}
                </a-avatar>
                <div class="crop-text">
                  <div class="crop-name">{{ area.crop.name }}</div>
                  <div class="crop-stage">
                    <a-progress
                      :percent="area.crop.growth"
                      size="small"
                      :show-info="false"
                    />
                    <span class="stage-text">{{ area.crop.stage }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- 创建/编辑分区弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑分区' : '新建分区'"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="分区名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入分区名称" />
        </a-form-item>

        <a-form-item label="分区代码" name="code">
          <a-input v-model:value="form.code" placeholder="请输入分区代码" />
        </a-form-item>

        <a-form-item label="分区描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入分区描述"
            :rows="3"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="面积(m²)" name="area">
              <a-input-number
                v-model:value="form.area"
                :min="1"
                :max="10000"
                style="width: 100%"
                placeholder="请输入面积"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大容量" name="capacity">
              <a-input-number
                v-model:value="form.capacity"
                :min="1"
                :max="10000"
                style="width: 100%"
                placeholder="请输入最大容量"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="位置" name="location">
          <a-input v-model:value="form.location" placeholder="请输入分区位置" />
        </a-form-item>

        <a-form-item label="负责人" name="manager">
          <a-select v-model:value="form.manager" placeholder="请选择负责人">
            <a-select-option value="张三">张三</a-select-option>
            <a-select-option value="李四">李四</a-select-option>
            <a-select-option value="王五">王五</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="环境配置" name="environmentConfig">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="目标温度(°C)">
                <a-input-number
                  v-model:value="form.targetTemp"
                  :min="10"
                  :max="40"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="目标湿度(%)">
                <a-input-number
                  v-model:value="form.targetHumidity"
                  :min="20"
                  :max="90"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  MoreOutlined,
  EditOutlined,
  EyeOutlined,
  ControlOutlined,
  DeleteOutlined,
  ThermometerOutlined,
  DropOutlined,
  BulbOutlined,
  CloudOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

// 数据状态
const loading = ref(false)
const submitLoading = ref(false)
const areas = ref([])

// 弹窗状态
const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 统计数据
const stats = reactive({
  totalAreas: 0,
  activeAreas: 0,
  maintenanceAreas: 0,
  totalCapacity: 0
})

// 表单数据
const form = reactive({
  name: '',
  code: '',
  description: '',
  area: undefined,
  capacity: undefined,
  location: '',
  manager: undefined,
  targetTemp: 25,
  targetHumidity: 60
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分区名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分区代码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '分区代码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入最大容量', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入位置', trigger: 'blur' }
  ],
  manager: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ]
}

// 获取设备图标
const getDeviceIcon = (type) => {
  const icons = {
    fan: 'FanOutlined',
    light: 'BulbOutlined',
    pump: 'DropOutlined',
    heater: 'FireOutlined',
    cooler: 'SnowOutlined'
  }
  return icons[type] || 'SettingOutlined'
}

// 生成模拟数据
const generateMockData = () => {
  return [
    {
      id: 1,
      name: 'A区 - 叶菜类种植区',
      code: 'AREA_A',
      description: '主要用于种植绿叶蔬菜',
      area: 500,
      capacity: 1000,
      location: '东区一楼',
      manager: '张三',
      online: true,
      temperature: 23.5,
      humidity: 65,
      light: 12000,
      co2: 450,
      devices: [
        { id: 1, name: '通风扇', type: 'fan', online: true, running: true },
        { id: 2, name: 'LED灯', type: 'light', online: true, running: true },
        { id: 3, name: '水泵', type: 'pump', online: true, running: false }
      ],
      crop: {
        name: '生菜',
        stage: '生长期 (65%)',
        growth: 65,
        image: ''
      }
    },
    {
      id: 2,
      name: 'B区 - 果蔬类种植区',
      code: 'AREA_B',
      description: '主要用于种植番茄、黄瓜等果蔬',
      area: 800,
      capacity: 1500,
      location: '西区二楼',
      manager: '李四',
      online: true,
      temperature: 26.8,
      humidity: 70,
      light: 15000,
      co2: 520,
      devices: [
        { id: 4, name: '加热器', type: 'heater', online: true, running: false },
        { id: 5, name: 'LED灯', type: 'light', online: true, running: true },
        { id: 6, name: '水泵', type: 'pump', online: false, running: false }
      ],
      crop: {
        name: '番茄',
        stage: '开花期 (45%)',
        growth: 45,
        image: ''
      }
    },
    {
      id: 3,
      name: 'C区 - 育苗区',
      code: 'AREA_C',
      description: '专门用于培育幼苗',
      area: 300,
      capacity: 500,
      location: '南区一楼',
      manager: '王五',
      online: false,
      temperature: 28.2,
      humidity: 80,
      light: 8000,
      co2: 480,
      devices: [
        { id: 7, name: '冷却器', type: 'cooler', online: false, running: false },
        { id: 8, name: 'LED灯', type: 'light', online: true, running: true },
        { id: 9, name: '水泵', type: 'pump', online: false, running: false }
      ],
      crop: {
        name: '黄瓜苗',
        stage: '育苗期 (25%)',
        growth: 25,
        image: ''
      }
    }
  ]
}

// 加载分区数据
const loadAreas = () => {
  loading.value = true
  setTimeout(() => {
    areas.value = generateMockData()

    // 更新统计数据
    stats.totalAreas = areas.value.length
    stats.activeAreas = areas.value.filter(area => area.online).length
    stats.maintenanceAreas = areas.value.filter(area => !area.online).length
    stats.totalCapacity = areas.value.reduce((sum, area) => sum + area.area, 0)

    loading.value = false
  }, 1000)
}

// 刷新数据
const handleRefresh = () => {
  loadAreas()
  message.success('数据刷新成功')
}

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

// 编辑分区
const editArea = (area) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(form, {
    ...area,
    targetTemp: area.temperature,
    targetHumidity: area.humidity
  })
}

// 查看分区详情
const viewAreaDetails = (area) => {
  message.info(`查看分区 ${area.name} 的详细信息`)
}

// 控制分区
const controlArea = (area) => {
  message.info(`控制分区 ${area.name}`)
}

// 删除分区
const deleteArea = (area) => {
  message.success(`分区 ${area.name} 删除成功`)
  loadAreas()
}

// 切换设备状态
const toggleDevice = (deviceId, checked) => {
  message.info(`设备 ${deviceId} 已${checked ? '开启' : '关闭'}`)
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    setTimeout(() => {
      if (isEdit.value) {
        const index = areas.value.findIndex(a => a.id === form.id)
        if (index > -1) {
          areas.value[index] = {
            ...areas.value[index],
            ...form
          }
        }
        message.success('分区更新成功')
      } else {
        const newArea = {
          id: Date.now(),
          ...form,
          online: true,
          temperature: 25,
          humidity: 60,
          light: 10000,
          co2: 400,
          devices: [],
          crop: {
            name: '暂无作物',
            stage: '准备期 (0%)',
            growth: 0,
            image: ''
          }
        }
        areas.value.push(newArea)
        message.success('分区创建成功')
      }

      modalVisible.value = false
      submitLoading.value = false
      resetForm()
      loadAreas()
    }, 1000)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 取消弹窗
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    description: '',
    area: undefined,
    capacity: undefined,
    location: '',
    manager: undefined,
    targetTemp: 25,
    targetHumidity: 60
  })
  formRef.value?.resetFields()
}

// 组件挂载
onMounted(() => {
  loadAreas()
})
</script>

<style scoped>
.areas-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.content-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.areas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.area-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.area-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.area-offline {
  border-left: 4px solid #ff4d4f;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.area-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.area-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.environment-data {
  background: #fafafa;
  padding: 12px;
  border-radius: 6px;
}

.data-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.data-row:last-child {
  margin-bottom: 0;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-label {
  font-size: 12px;
  color: #8c8c8c;
}

.data-value {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: #262626;
}

.device-status h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #595959;
}

.devices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  gap: 4px;
}

.device-offline {
  opacity: 0.5;
  background: #ffe7e7;
}

.device-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.crop-info h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #595959;
}

.crop-details {
  display: flex;
  align-items: center;
  gap: 12px;
}

.crop-text {
  flex: 1;
}

.crop-name {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.crop-stage {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stage-text {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .content-container {
    padding: 16px;
  }

  .areas-grid {
    grid-template-columns: 1fr;
  }

  .data-row {
    flex-direction: column;
    gap: 8px;
  }

  .devices-grid {
    grid-template-columns: 1fr;
  }
}
</style>