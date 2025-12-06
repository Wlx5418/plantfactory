import { ref, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'

/**
 * WebSocket Composable
 * 提供WebSocket连接和消息处理功能
 */
export function useWebSocket() {
  const isConnected = ref(false)
  const connectionState = ref('disconnected') // disconnected, connecting, connected, error
  const lastMessage = ref(null)
  const messageCount = ref(0)
  let client = null

  const connectionUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws`

  const connect = () => {
    if (client && client.readyState === WebSocket.OPEN) {
      return
    }

    connectionState.value = 'connecting'

    try {
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('WebSocket连接失败: 缺少JWT令牌')
        connectionState.value = 'error'
        return
      }

      // 创建WebSocket连接，附带token
      const wsUrl = `${connectionUrl}?token=${encodeURIComponent(token)}`
      client = new WebSocket(wsUrl)

      client.onopen = (event) => {
        console.log('WebSocket连接成功:', event)
        isConnected.value = true
        connectionState.value = 'connected'
        message.success('实时连接已建立')
      }

      client.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          lastMessage.value = data
          messageCount.value++

          // 触发自定义事件，让组件可以监听消息
          window.dispatchEvent(new CustomEvent('websocket-message', {
            detail: data
          }))
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      client.onclose = (event) => {
        console.log('WebSocket连接关闭:', event)
        isConnected.value = false
        connectionState.value = 'disconnected'

        if (event.code !== 1000) {
          message.warning('实时连接已断开，正在尝试重新连接...')
          // 尝试重连
          setTimeout(() => {
            connect()
          }, 3000)
        }
      }

      client.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        isConnected.value = false
        connectionState.value = 'error'
        message.error('实时连接发生错误')
      }

    } catch (error) {
      console.error('WebSocket连接初始化失败:', error)
      connectionState.value = 'error'
      message.error('无法建立实时连接')
    }
  }

  const disconnect = () => {
    if (client) {
      client.close(1000, 'Normal closure')
      client = null
    }
    isConnected.value = false
    connectionState.value = 'disconnected'
  }

  const send = (data) => {
    if (client && client.readyState === WebSocket.OPEN) {
      try {
        const message = typeof data === 'string' ? data : JSON.stringify(data)
        client.send(message)
        return true
      } catch (error) {
        console.error('发送WebSocket消息失败:', error)
        message.error('发送消息失败')
      }
    } else {
      console.warn('WebSocket未连接，无法发送消息')
      message.warning('连接已断开，无法发送消息')
    }
    return false
  }

  const subscribe = (topic, callback) => {
    const messageHandler = (event) => {
      if (event.detail && event.detail.type === topic) {
        callback(event.detail)
      }
    }

    window.addEventListener('websocket-message', messageHandler)

    // 返回取消订阅函数
    return () => {
      window.removeEventListener('websocket-message', messageHandler)
    }
  }

  const unsubscribe = (topic) => {
    // 在实际应用中，这里可能需要维护一个订阅列表
    console.log('取消订阅主题:', topic)
  }

  return {
    // 状态
    isConnected,
    connectionState,
    lastMessage,
    messageCount,

    // 方法
    connect,
    disconnect,
    send,
    subscribe,
    unsubscribe
  }
}

/**
 * 环境数据WebSocket Composable
 * 专门用于处理环境数据的实时更新
 */
export function useEnvironmentWebSocket() {
  const { isConnected, subscribe, unsubscribe } = useWebSocket()
  const environmentData = ref({})
  const alerts = ref([])

  const handleEnvironmentData = (data) => {
    if (data.type === 'environment_data') {
      environmentData.value = data.payload
    } else if (data.type === 'alert') {
      alerts.value.unshift(data.payload)
      // 限制告警数量
      if (alerts.value.length > 50) {
        alerts.value = alerts.value.slice(0, 50)
      }
    }
  }

  onMounted(() => {
    subscribe('environment', handleEnvironmentData)
  })

  onUnmounted(() => {
    unsubscribe('environment')
  })

  return {
    isConnected,
    environmentData,
    alerts
  }
}