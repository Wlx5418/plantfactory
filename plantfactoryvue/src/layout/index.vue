<template>
  <a-layout class="layout-container">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="sidebarCollapsed"
      :collapsible="true"
      :trigger="null"
      class="layout-sider"
      :class="{ 'layout-sider-mobile': isMobile }"
    >
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" />
        <span v-if="!sidebarCollapsed" class="logo-text">植物工厂</span>
      </div>

      <AppMenu :mode="isMobile ? 'vertical' : 'inline'" :theme="theme === 'dark' ? 'dark' : 'light'" />
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout class="layout-main">
      <!-- 头部 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <a-button
            type="text"
            class="trigger"
            @click="toggleSidebar"
          >
            <MenuUnfoldOutlined v-if="sidebarCollapsed" />
            <MenuFoldOutlined v-else />
          </a-button>

          <!-- 面包屑导航 -->
          <a-breadcrumb v-if="showBreadcrumb" class="breadcrumb">
            <a-breadcrumb-item>
              <HomeOutlined />
              <span style="margin-left: 4px">首页</span>
            </a-breadcrumb-item>
            <a-breadcrumb-item v-for="item in breadcrumbList" :key="item.path">
              {{ item.meta.title }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 主题切换 -->
          <a-button
            type="text"
            class="header-action"
            @click="toggleTheme"
          >
            <BulbOutlined />
          </a-button>

          <!-- 全屏切换 -->
          <a-button
            type="text"
            class="header-action"
            @click="toggleFullscreen"
          >
            <FullscreenOutlined v-if="!isFullscreen" />
            <FullscreenExitOutlined v-else />
          </a-button>

          <!-- 用户信息 -->
          <a-dropdown placement="bottomRight">
            <div class="user-info">
              <a-avatar :src="userInfo.avatarUrl" class="user-avatar">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <span class="user-name">{{ displayName }}</span>
              <DownOutlined />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="goToProfile">
                  <UserOutlined />
                  个人中心
                </a-menu-item>
                <a-menu-item @click="goToSettings">
                  <SettingOutlined />
                  系统设置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 标签页导航 -->
      <div v-if="showTabs" class="layout-tabs">
        <a-tabs
          v-model:activeKey="activeTabKey"
          type="editable-card"
          :hide-add="true"
          class="tabs-nav"
          @tabClick="handleTabClick"
          @edit="handleTabEdit"
        >
          <a-tab-pane
            v-for="tab in tabs"
            :key="tab.key"
            :tab="tab.title"
            :closable="tabs.length > 1"
          />
        </a-tabs>
      </div>

      <!-- 页面内容 -->
      <a-layout-content class="layout-content">
        <div class="content-wrapper">
          <router-view />
          <!-- 或者带过渡效果的写法 -->
          <!--
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
          -->
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'ant-design-vue'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
  BulbOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined
} from '@ant-design/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import AppMenu from './components/AppMenu.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 响应式数据
const isMobile = ref(false)
const isFullscreen = ref(false)

// 计算属性
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const theme = computed(() => appStore.theme)
const showBreadcrumb = computed(() => appStore.settings.showBreadcrumb)
const showTabs = computed(() => appStore.settings.showTabs)
const tabs = computed(() => appStore.tabs)
const activeTabKey = computed(() => appStore.activeTabKey)
const userInfo = computed(() => userStore.userInfo)
const displayName = computed(() => userStore.displayName)


// 面包屑列表
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.filter(item => item.path !== '/' && item.meta.title !== '仪表盘')
})

// 方法
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const toggleTheme = () => {
  const newTheme = theme.value === 'light' ? 'dark' : 'light'
  appStore.setTheme(newTheme)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}


const handleTabClick = (key) => {
  router.push(key)
}

const handleTabEdit = (targetKey, action) => {
  if (action === 'remove') {
    appStore.removeTab(targetKey)
  }
}

const goToProfile = () => {
  router.push('/profile')
}

const goToSettings = () => {
  router.push('/system/settings')
}

const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '您确定要退出系统吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await userStore.logoutAction()
      router.push('/login')
    }
  })
}

// 监听路由变化，更新标签页
watch(route, (newRoute) => {
  if (newRoute.meta?.title) {
    // 添加标签页
    if (showTabs.value) {
      appStore.addTab({
        key: newRoute.path,
        title: newRoute.meta.title,
        content: newRoute.path
      })
    }
  }
}, { immediate: true })

// 检查设备类型
const checkDeviceType = () => {
  const width = window.innerWidth
  isMobile.value = width < 768
  appStore.setDevice(isMobile.value ? 'mobile' : 'desktop')
}

onMounted(() => {
  checkDeviceType()
  window.addEventListener('resize', checkDeviceType)
})
</script>

<style scoped lang="less">
.layout-container {
  min-height: 100vh;
}

.layout-sider {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;

  &.layout-sider-mobile {
    transform: translateX(-100%);

    &.ant-layout-sider-collapsed {
      transform: translateX(0);
    }
  }

  .logo {
    height: 64px;
    padding: 16px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;

    img {
      width: 32px;
      height: 32px;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #1890ff;
    }
  }

  .layout-menu {
    border-right: none;
    height: calc(100vh - 64px);
    overflow-y: auto;
  }
}

.layout-main {
  margin-left: 200px;
  transition: margin-left 0.2s;

  .layout-sider.ant-layout-sider-collapsed + & {
    margin-left: 80px;
  }
}

.layout-header {
  background: #fff;
  padding: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 99;

  .header-left {
    display: flex;
    align-items: center;

    .trigger {
      font-size: 18px;
      padding: 0 16px;
      height: 64px;
      border-right: 1px solid #f0f0f0;
    }

    .breadcrumb {
      margin-left: 16px;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    padding-right: 24px;

    .header-action {
      margin-left: 8px;
      font-size: 16px;
      padding: 0 8px;
      height: 64px;
      border: none;

      &:hover {
        color: #1890ff;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 0 12px;
      height: 64px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f5f5;
      }

      .user-avatar {
        margin-right: 8px;
      }

      .user-name {
        margin: 0 8px;
      }
    }
  }
}

.layout-tabs {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  .tabs-nav {
    margin: 0;

    :deep(.ant-tabs-bar) {
      margin: 0;
      border: none;
    }

    :deep(.ant-tabs-tab) {
      margin: 0;
      border-right: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;
      background: #fafafa;

      &.ant-tabs-tab-active {
        background: #fff;
        border-bottom: 1px solid #fff;
      }
    }
  }
}

.layout-content {
  min-height: calc(100vh - 64px - 46px);

  .content-wrapper {
    padding: 24px;
    background: #f0f2f5;
    min-height: calc(100vh - 64px - 46px);
  }
}

// 页面切换动画
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

// 响应式设计
@media (max-width: 768px) {
  .layout-main {
    margin-left: 0 !important;
  }

  .layout-header {
    padding: 0 16px;

    .header-right {
      padding-right: 0;

      .user-name {
        display: none;
      }
    }
  }

  .layout-content {
    .content-wrapper {
      padding: 16px;
    }
  }
}
</style>