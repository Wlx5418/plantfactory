import { message } from 'ant-design-vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * WebSocket管理类
 * 支持JWT认证、指数退避重连、心跳检测
 */
class WebSocketManager {
  constructor() {
    this.client = null
    this.connectionCount = 0
    this.maxReconnectAttempts = 10
    this.reconnectDelay = 1000 // 初始重连延迟1秒
    this.maxReconnectDelay = 30000 // 最大重连延迟30秒
    this.heartbeatInterval = 30000 // 心跳间隔30秒
    this.heartbeatTimer = null
    this.subscriptions = new Map()
    this.isConnected = false
    this.reconnectTimer = null
  }

  /**
   * 连接WebSocket
   * @param {Object} options 连接选项
   */
  connect(options = {}) {
    const {
      onConnect = this.onConnect,
      onDisconnect = this.onDisconnect,
      onError = this.onError,
      onMessage = this.onMessage
    } = options

    try {
      // 获取JWT令牌
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('WebSocket连接失败: 缺少JWT令牌')
        message.error('WebSocket连接失败: 未登录')
        return
      }

      // 创建STOMP客户端
      this.client = new Client({
        webSocketFactory: () => {
          return new SockJS('/api/ws/environment', null, {
            // 传递JWT令牌
            headers: {
              Authorization: `Bearer ${token}`
            }
          })
        },
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => {
          console.log('STOMP Debug:', str)
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.isConnected = true
          this.connectionCount = 0
          this.reconnectDelay = 1000 // 重置重连延迟

          // 开始心跳
          this.startHeartbeat()

          // 重新订阅之前的主题
          this.resubscribeAll()

          onConnect(frame)
        },
        onDisconnect: (frame) => {
          console.log('WebSocket连接断开:', frame)
          this.isConnected = false
          this.stopHeartbeat()

          onDisconnect(frame)

          // 尝试重连
          if (this.connectionCount < this.maxReconnectAttempts) {
            this.scheduleReconnect()
          }
        },
        onStompError: (frame) => {
          console.error('STOMP错误:', frame)
          this.isConnected = false

          // 检查是否为认证错误
          if (frame.headers.message && frame.headers.message.includes('401')) {
            message.error('WebSocket认证失败，请重新登录')
            localStorage.removeItem('token')
            window.location.href = '/login'
          } else {
            message.error('WebSocket连接错误: ' + (frame.headers.message || '未知错误'))
          }

          onError(frame)
        }
      })

      // 处理消息
      this.client.onWebSocketMessage = (message) => {
        onMessage(message)
      }

      // 激活连接
      this.client.activate()

    } catch (error) {
      console.error('WebSocket连接初始化失败:', error)
      message.error('WebSocket连接初始化失败')
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.client) {
      this.client.deactivate()
      this.client = null
    }

    this.isConnected = false
    this.subscriptions.clear()
    console.log('WebSocket连接已断开')
  }

  /**
   * 订阅主题
   * @param {string} destination 主题地址
   * @param {Function} callback 消息回调
   * @param {Object} headers 订阅头部
   * @returns {Object} 订阅对象
   */
  subscribe(destination, callback, headers = {}) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket未连接，无法订阅主题:', destination)
      return null
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body)
        callback(data, message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
        callback(message.body, message)
      }
    }, headers)

    // 保存订阅信息
    this.subscriptions.set(destination, {
      subscription,
      callback,
      headers
    })

    console.log('已订阅主题:', destination)
    return subscription
  }

  /**
   * 取消订阅
   * @param {string} destination 主题地址
   */
  unsubscribe(destination) {
    const subInfo = this.subscriptions.get(destination)
    if (subInfo && subInfo.subscription) {
      subInfo.subscription.unsubscribe()
      this.subscriptions.delete(destination)
      console.log('已取消订阅主题:', destination)
    }
  }

  /**
   * 发送消息
   * @param {string} destination 目标地址
   * @param {Object} body 消息体
   * @param {Object} headers 消息头部
   */
  send(destination, body, headers = {}) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket未连接，无法发送消息')
      return
    }

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
        headers
      })
      console.log('消息已发送到:', destination)
    } catch (error) {
      console.error('发送消息失败:', error)
    }
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected && this.client) {
        // 发送心跳消息
        this.send('/app/heartbeat', {
          timestamp: new Date().toISOString(),
          type: 'heartbeat'
        })
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 安排重连（指数退避）
   */
  scheduleReconnect() {
    if (this.reconnectTimer) {
      return
    }

    const delay = Math.min(
      this.reconnectDelay * Math.pow(2, this.connectionCount),
      this.maxReconnectDelay
    )

    this.connectionCount++
    console.log(`将在${delay}ms后尝试第${this.connectionCount}次重连`)

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, delay)
  }

  /**
   * 重新订阅所有主题
   */
  resubscribeAll() {
    const subscriptions = Array.from(this.subscriptions.entries())

    this.subscriptions.clear()

    subscriptions.forEach(([destination, subInfo]) => {
      this.subscribe(destination, subInfo.callback, subInfo.headers)
    })
  }

  /**
   * 默认连接成功回调
   */
  onConnect(frame) {
    console.log('WebSocket连接已建立')
  }

  /**
   * 默认连接断开回调
   */
  onDisconnect(frame) {
    console.log('WebSocket连接已断开')
  }

  /**
   * 默认错误回调
   */
  onError(frame) {
    console.error('WebSocket发生错误')
  }

  /**
   * 默认消息回调
   */
  onMessage(message) {
    console.log('收到WebSocket消息:', message)
  }

  /**
   * 获取连接状态
   */
  getConnectionState() {
    return {
      isConnected: this.isConnected,
      connectionCount: this.connectionCount,
      subscriptionsCount: this.subscriptions.size
    }
  }
}

// 创建单例实例
const websocketManager = new WebSocketManager()

export default websocketManager