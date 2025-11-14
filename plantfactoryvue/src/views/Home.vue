<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">🌱</span>
          <span v-if="!sidebarCollapsed" class="logo-text">植物工厂</span>
        </div>
        <button class="sidebar-toggle" @click="toggleSidebar">
          <span v-if="sidebarCollapsed">☰</span>
          <span v-else>☰</span>
        </button>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-title">主要功能</div>
          <a href="#" class="nav-item active" @click.prevent>
            <span class="nav-icon">🏠</span>
            <span v-if="!sidebarCollapsed" class="nav-text">仪表盘</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">🏭</span>
            <span v-if="!sidebarCollapsed" class="nav-text">生产管理</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">🌡️</span>
            <span v-if="!sidebarCollapsed" class="nav-text">环境监控</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">📊</span>
            <span v-if="!sidebarCollapsed" class="nav-text">数据分析</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">📦</span>
            <span v-if="!sidebarCollapsed" class="nav-text">库存管理</span>
          </a>
        </div>

        <div class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-title">系统管理</div>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">👥</span>
            <span v-if="!sidebarCollapsed" class="nav-text">用户管理</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">⚙️</span>
            <span v-if="!sidebarCollapsed" class="nav-text">系统设置</span>
          </a>
          <a href="#" class="nav-item" @click.prevent>
            <span class="nav-icon">📄</span>
            <span v-if="!sidebarCollapsed" class="nav-text">报表中心</span>
          </a>
        </div>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航 -->
      <header class="top-header">
        <div class="header-left">
          <h1>仪表盘概览</h1>
          <p class="breadcrumb">首页 / 仪表盘</p>
        </div>

        <div class="header-right">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input type="text" placeholder="搜索功能..." />
          </div>

          <div class="notification-bell">
            <span>🔔</span>
            <span class="notification-dot"></span>
          </div>

          <div class="user-dropdown" @click="toggleUserMenu">
            <div class="user-avatar">
              <span>{{ userInfo.realName?.[0] || userInfo.username?.[0] || 'A' }}</span>
            </div>
            <div class="user-info">
              <div class="user-name">{{ userInfo.realName || userInfo.username }}</div>
              <div class="user-role">管理员</div>
            </div>
            <span class="dropdown-arrow">▼</span>

            <div v-if="showUserMenu" class="user-menu">
              <a href="#" class="menu-item">
                <span>👤</span> 个人资料
              </a>
              <a href="#" class="menu-item">
                <span>🔧</span> 账户设置
              </a>
              <div class="menu-divider"></div>
              <a href="#" class="menu-item logout" @click.prevent="handleLogout">
                <span>🚪</span> 退出登录
              </a>
            </div>
          </div>
        </div>
      </header>

      <!-- 仪表盘内容 -->
      <main class="dashboard-content">
        <!-- 欢迎区域 -->
        <section class="welcome-section">
          <div class="welcome-content">
            <h2>欢迎回来，{{ userInfo.realName || userInfo.username }}！</h2>
            <p>今天是 {{ currentDate }}，让我们开始管理您的植物工厂</p>
          </div>
          <div class="weather-widget">
            <span class="weather-icon">☀️</span>
            <div class="weather-info">
              <div class="temperature">26°C</div>
              <div class="weather-desc">适宜生长</div>
            </div>
          </div>
        </section>

        <!-- 统计卡片 -->
        <section class="stats-section">
          <div class="stats-grid">
            <div class="stat-card primary">
              <div class="stat-icon">🏭</div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.productionAreas }}</div>
                <div class="stat-label">生产区域</div>
                <div class="stat-change positive">+2 本月新增</div>
              </div>
            </div>

            <div class="stat-card success">
              <div class="stat-icon">🔌</div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.onlineDevices }}</div>
                <div class="stat-label">在线设备</div>
                <div class="stat-change positive">99% 运行率</div>
              </div>
            </div>

            <div class="stat-card warning">
              <div class="stat-icon">🌡️</div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.temperature }}°C</div>
                <div class="stat-label">当前温度</div>
                <div class="stat-change normal">适宜范围</div>
              </div>
            </div>

            <div class="stat-card info">
              <div class="stat-icon">📊</div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.dailyYield }}kg</div>
                <div class="stat-label">今日产量</div>
                <div class="stat-change positive">+12% 较昨日</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 快速操作和图表 -->
        <section class="content-grid">
          <!-- 快速操作 -->
          <div class="quick-actions">
            <h3>快速操作</h3>
            <div class="action-grid">
              <button class="action-btn">
                <span class="action-icon">➕</span>
                <span class="action-text">新增区域</span>
              </button>
              <button class="action-btn">
                <span class="action-icon">📋</span>
                <span class="action-text">生成报告</span>
              </button>
              <button class="action-btn">
                <span class="action-icon">🔄</span>
                <span class="action-text">设备维护</span>
              </button>
              <button class="action-btn">
                <span class="action-icon">⚠️</span>
                <span class="action-text">异常处理</span>
              </button>
            </div>
          </div>

          <!-- 最近活动 -->
          <div class="recent-activities">
            <h3>最近活动</h3>
            <div class="activity-list">
              <div class="activity-item">
                <div class="activity-icon success">✓</div>
                <div class="activity-content">
                  <div class="activity-title">区域A温度调节完成</div>
                  <div class="activity-time">2分钟前</div>
                </div>
              </div>
              <div class="activity-item">
                <div class="activity-icon warning">⚠</div>
                <div class="activity-content">
                  <div class="activity-title">区域B湿度异常</div>
                  <div class="activity-time">15分钟前</div>
                </div>
              </div>
              <div class="activity-item">
                <div class="activity-icon info">📊</div>
                <div class="activity-content">
                  <div class="activity-title">日产量报告已生成</div>
                  <div class="activity-time">1小时前</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 系统状态 -->
        <section class="system-status">
          <h3>系统状态</h3>
          <div class="status-grid">
            <div class="status-item">
              <div class="status-indicator online"></div>
              <span class="status-label">数据库连接</span>
              <span class="status-value">正常</span>
            </div>
            <div class="status-item">
              <div class="status-indicator online"></div>
              <span class="status-label">传感器网络</span>
              <span class="status-value">12/12 在线</span>
            </div>
            <div class="status-item">
              <div class="status-indicator warning"></div>
              <span class="status-label">存储空间</span>
              <span class="status-value">78% 使用</span>
            </div>
            <div class="status-item">
              <div class="status-indicator online"></div>
              <span class="status-label">备份系统</span>
              <span class="status-value">上次: 2小时前</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const sidebarCollapsed = ref(false)
const showUserMenu = ref(false)
const userInfo = ref({})
const currentDate = ref('')

const stats = ref({
  productionAreas: 5,
  onlineDevices: 12,
  temperature: 24,
  dailyYield: 150
})

onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }

  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 1000;
}

.sidebar-collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  font-size: 18px;
}

.logo-icon {
  font-size: 24px;
}

.sidebar-toggle {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  padding: 5px;
  border-radius: 5px;
  transition: background 0.3s;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 30px;
}

.nav-title {
  padding: 0 20px 10px;
  font-size: 12px;
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: white;
}

.nav-icon {
  font-size: 18px;
  min-width: 24px;
}

.nav-text {
  margin-left: 10px;
  white-space: nowrap;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  margin-left: 260px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
}

.sidebar-collapsed + .main-content {
  margin-left: 70px;
}

/* 顶部导航 */
.top-header {
  background: white;
  padding: 20px 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
  margin: 5px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #999;
}

.search-box input {
  padding: 10px 15px 10px 40px;
  border: 1px solid #e0e0e0;
  border-radius: 25px;
  width: 250px;
  outline: none;
  transition: border-color 0.3s;
}

.search-box input:focus {
  border-color: #667eea;
}

.notification-bell {
  position: relative;
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.notification-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 25px;
  cursor: pointer;
  transition: background 0.3s;
  position: relative;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}

.user-info {
  text-align: right;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.user-role {
  font-size: 12px;
  color: #666;
}

.dropdown-arrow {
  font-size: 12px;
  color: #666;
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 1000;
  margin-top: 10px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  transition: background 0.3s;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.logout {
  color: #ff4d4f;
}

.menu-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 4px 0;
}

/* 仪表盘内容 */
.dashboard-content {
  padding: 30px;
  flex: 1;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.3);
}

.welcome-content h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.weather-widget {
  display: flex;
  align-items: center;
  gap: 15px;
  background: rgba(255, 255, 255, 0.2);
  padding: 15px 20px;
  border-radius: 10px;
}

.weather-icon {
  font-size: 40px;
}

.temperature {
  font-size: 24px;
  font-weight: bold;
}

.weather-desc {
  font-size: 14px;
  opacity: 0.9;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 40px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive {
  color: #52c41a;
}

.stat-change.normal {
  color: #fa8c16;
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.quick-actions,
.recent-activities {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.quick-actions h3,
.recent-activities h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #f5f5f5;
  border-color: #667eea;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 24px;
}

.action-text {
  font-size: 14px;
  color: #666;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.activity-icon.success {
  background: #52c41a;
}

.activity-icon.warning {
  background: #fa8c16;
}

.activity-icon.info {
  background: #13c2c2;
}

.activity-content {
  flex: 1;
}

.activity-title {
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.activity-time {
  color: #999;
  font-size: 12px;
}

/* 系统状态 */
.system-status {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.system-status h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
}

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-indicator.online {
  background: #52c41a;
}

.status-indicator.warning {
  background: #fa8c16;
}

.status-indicator.offline {
  background: #ff4d4f;
}

.status-label {
  flex: 1;
  color: #666;
  font-size: 14px;
}

.status-value {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.show {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .header-right {
    gap: 10px;
  }

  .search-box input {
    width: 150px;
  }

  .user-info {
    display: none;
  }
}

@media (max-width: 480px) {
  .dashboard-content {
    padding: 20px;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .status-grid {
    grid-template-columns: 1fr;
  }
}
</style>