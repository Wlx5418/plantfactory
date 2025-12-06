<template>
  <div class="monitor-settings">
    <a-form
      :model="formSettings"
      :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }"
      @finish="handleSave"
    >
      <a-divider orientation="left">环境参数阈值</a-divider>

      <a-form-item label="温度范围" required>
        <a-input-group compact>
          <a-input-number
            v-model:value="formSettings.temperature.min"
            :min="0"
            :max="50"
            style="width: 100px"
            placeholder="最低"
          />
          <a-input
            style="width: 30px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="~"
            disabled
          />
          <a-input-number
            v-model:value="formSettings.temperature.max"
            :min="0"
            :max="50"
            style="width: 100px; border-left: 0"
            placeholder="最高"
          />
          <a-input
            style="width: 50px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="°C"
            disabled
          />
        </a-input-group>
      </a-form-item>

      <a-form-item label="湿度范围" required>
        <a-input-group compact>
          <a-input-number
            v-model:value="formSettings.humidity.min"
            :min="0"
            :max="100"
            style="width: 100px"
            placeholder="最低"
          />
          <a-input
            style="width: 30px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="~"
            disabled
          />
          <a-input-number
            v-model:value="formSettings.humidity.max"
            :min="0"
            :max="100"
            style="width: 100px; border-left: 0"
            placeholder="最高"
          />
          <a-input
            style="width: 50px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="%"
            disabled
          />
        </a-input-group>
      </a-form-item>

      <a-form-item label="光照强度范围" required>
        <a-input-group compact>
          <a-input-number
            v-model:value="formSettings.lightIntensity.min"
            :min="0"
            :max="20000"
            style="width: 100px"
            placeholder="最低"
          />
          <a-input
            style="width: 30px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="~"
            disabled
          />
          <a-input-number
            v-model:value="formSettings.lightIntensity.max"
            :min="0"
            :max="20000"
            style="width: 100px; border-left: 0"
            placeholder="最高"
          />
          <a-input
            style="width: 60px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="lux"
            disabled
          />
        </a-input-group>
      </a-form-item>

      <a-form-item label="CO₂浓度范围" required>
        <a-input-group compact>
          <a-input-number
            v-model:value="formSettings.co2Level.min"
            :min="0"
            :max="2000"
            style="width: 100px"
            placeholder="最低"
          />
          <a-input
            style="width: 30px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="~"
            disabled
          />
          <a-input-number
            v-model:value="formSettings.co2Level.max"
            :min="0"
            :max="2000"
            style="width: 100px; border-left: 0"
            placeholder="最高"
          />
          <a-input
            style="width: 60px; text-align: center; border-left: 0; pointer-events: none; background-color: #fff"
            placeholder="ppm"
            disabled
          />
        </a-input-group>
      </a-form-item>

      <a-divider orientation="left">监控设置</a-divider>

      <a-form-item label="告警检查间隔" required>
        <a-input-number
          v-model:value="formSettings.alertInterval"
          :min="1"
          :max="60"
          :step="1"
          style="width: 120px"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">分钟</span>
      </a-form-item>

      <a-form-item label="数据采集间隔" required>
        <a-input-number
          v-model:value="formSettings.dataInterval"
          :min="10"
          :max="300"
          :step="10"
          style="width: 120px"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">秒</span>
      </a-form-item>

      <a-form-item label="自动控制">
        <a-switch v-model:checked="formSettings.autoControl" />
        <span style="margin-left: 8px; color: #8c8c8c;">
          启用后系统将自动调节环境参数
        </span>
      </a-form-item>

      <a-form-item label="异常通知">
        <a-switch v-model:checked="formSettings.enableNotification" />
        <span style="margin-left: 8px; color: #8c8c8c;">
          环境参数异常时发送通知
        </span>
      </a-form-item>

      <a-form-item label="通知方式" v-if="formSettings.enableNotification">
        <a-checkbox-group v-model:value="formSettings.notificationMethods">
          <a-checkbox value="browser">浏览器通知</a-checkbox>
          <a-checkbox value="email">邮件通知</a-checkbox>
          <a-checkbox value="sms">短信通知</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
        <a-space>
          <a-button type="primary" html-type="submit" :loading="saving">
            保存设置
          </a-button>
          <a-button @click="handleCancel">
            取消
          </a-button>
          <a-button @click="handleReset">
            重置默认
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'

const props = defineProps({
  settings: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['save', 'cancel'])

const saving = ref(false)

const formSettings = reactive({
  temperature: { min: 20, max: 28 },
  humidity: { min: 60, max: 80 },
  lightIntensity: { min: 3000, max: 8000 },
  co2Level: { min: 600, max: 1200 },
  alertInterval: 5,
  dataInterval: 30,
  autoControl: false,
  enableNotification: true,
  notificationMethods: ['browser']
})

const defaultSettings = {
  temperature: { min: 20, max: 28 },
  humidity: { min: 60, max: 80 },
  lightIntensity: { min: 3000, max: 8000 },
  co2Level: { min: 600, max: 1200 },
  alertInterval: 5,
  dataInterval: 30,
  autoControl: false,
  enableNotification: true,
  notificationMethods: ['browser']
}

// 监听props变化，更新表单数据
watch(() => props.settings, (newSettings) => {
  Object.assign(formSettings, defaultSettings, newSettings)
}, { immediate: true, deep: true })

const handleSave = async () => {
  saving.value = true
  try {
    // 验证设置的有效性
    if (!validateSettings()) {
      return
    }

    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    emit('save', { ...formSettings })
    message.success('设置保存成功')
  } catch (error) {
    message.error('设置保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  // 恢复原始设置
  Object.assign(formSettings, props.settings)
  emit('cancel')
}

const handleReset = () => {
  Object.assign(formSettings, defaultSettings)
  message.info('已重置为默认设置')
}

const validateSettings = () => {
  const { temperature, humidity, lightIntensity, co2Level } = formSettings

  if (temperature.min >= temperature.max) {
    message.error('温度范围设置无效')
    return false
  }

  if (humidity.min >= humidity.max) {
    message.error('湿度范围设置无效')
    return false
  }

  if (lightIntensity.min >= lightIntensity.max) {
    message.error('光照强度范围设置无效')
    return false
  }

  if (co2Level.min >= co2Level.max) {
    message.error('CO₂浓度范围设置无效')
    return false
  }

  return true
}
</script>

<style scoped lang="less">
.monitor-settings {
  .ant-divider {
    margin: 16px 0;
  }

  :deep(.ant-form-item) {
    margin-bottom: 16px;
  }

  .ant-input-group .ant-input {
    text-align: center;
  }
}
</style>