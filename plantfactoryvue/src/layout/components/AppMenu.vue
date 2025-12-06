<template>
  <a-menu
    v-model:selectedKeys="selectedKeys"
    v-model:openKeys="openKeys"
    :mode="mode"
    :theme="theme"
    class="layout-menu"
    @click="handleMenuClick"
  >
    <template v-for="route in menuRoutes" :key="route.path">
      <!-- 有子菜单的情况 -->
      <a-sub-menu v-if="route.children && route.children.length > 0" :key="route.path">
        <template #icon>
          <component :is="getIcon(route.meta.icon)" />
        </template>
        <template #title>{{ route.meta.title }}</template>
        <a-menu-item
          v-for="child in route.children.filter(child => !child.meta?.hideInMenu)"
          :key="child.path.startsWith('/') ? child.path : `/${route.path}/${child.path}`"
        >
          <template #icon v-if="child.meta?.icon">
            <component :is="getIcon(child.meta.icon)" />
          </template>
          {{ child.meta.title }}
        </a-menu-item>
      </a-sub-menu>

      <!-- 单个菜单项 -->
      <a-menu-item v-else :key="route.path">
        <template #icon>
          <component :is="getIcon(route.meta.icon)" />
        </template>
        {{ route.meta.title }}
      </a-menu-item>
    </template>
  </a-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  DashboardOutlined,
  MonitorOutlined,
  UserOutlined,
  ApartmentOutlined,
  BarChartOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 菜单配置
const iconMap = {
  dashboard: DashboardOutlined,
  monitor: MonitorOutlined,
  user: UserOutlined,
  apartment: ApartmentOutlined,
  'bar-chart': BarChartOutlined,
  setting: SettingOutlined
}

const props = defineProps({
  mode: {
    type: String,
    default: 'inline'
  },
  theme: {
    type: String,
    default: 'light'
  }
})

// 计算属性
const selectedKeys = computed(() => {
  // 获取当前选中的菜单项
  const path = route.path
  const matched = route.matched

  // 查找最匹配的菜单项
  for (let i = matched.length - 1; i >= 0; i--) {
    const item = matched[i]
    if (item.meta?.title && !item.meta?.hideInMenu) {
      return [item.path]
    }
  }
  return [path]
})

const openKeys = computed(() => {
  // 获取当前应该展开的父菜单
  const path = route.path
  const openKeys = []

  // 遍历菜单路由，找到匹配的父菜单
  menuRoutes.value.forEach(route => {
    if (route.children && route.children.some(child => path.startsWith(child.path))) {
      openKeys.push(route.path)
    }
  })

  return openKeys
})

// 过滤后的菜单路由
const menuRoutes = computed(() => {
  const routes = router.options.routes.find(r => r.path === '/')?.children || []

  return routes.filter(route => {
    // 过滤掉隐藏的菜单
    if (route.meta?.hideInMenu) return false

    // 检查权限
    if (route.meta?.roles) {
      return route.meta.roles.some(role => userStore.permissions.includes(role)) || userStore.permissions.includes('ADMIN')
    }

    return true
  }).map(route => {
    // 处理子菜单
    if (route.children) {
      const processedChildren = route.children.filter(child => {
        if (child.meta?.hideInMenu) return false

        if (child.meta?.roles) {
          return child.meta.roles.some(role => userStore.permissions.includes(role)) || userStore.permissions.includes('ADMIN')
        }

        return true
      }).map(child => {
        // 检查子路由是否已经有完整路径
        let fullPath = child.path
        if (!child.path.startsWith('/')) {
          fullPath = `/${route.path}/${child.path}`
        }

        return {
          ...child,
          path: fullPath
        }
      })

      return {
        ...route,
        children: processedChildren
      }
    }
    return route
  })
})

// 方法
const getIcon = (iconName) => {
  return iconMap[iconName] || DashboardOutlined
}

const handleMenuClick = ({ key }) => {
  // 处理菜单点击
  if (key !== route.path) {
    router.push(key)
  }
}
</script>

<style scoped lang="less">
.layout-menu {
  border-right: none;
  height: calc(100vh - 64px);
  overflow-y: auto;

  :deep(.ant-menu-item) {
    margin: 4px 8px;
    border-radius: 6px;

    &.ant-menu-item-selected {
      background-color: #e6f7ff;

      &::after {
        display: none;
      }
    }
  }

  :deep(.ant-menu-submenu) {
    margin: 4px 8px;
    border-radius: 6px;

    > .ant-menu-submenu-title {
      border-radius: 6px;

      &:hover {
        background-color: #f5f5f5;
      }
    }

    &.ant-menu-submenu-open > .ant-menu-submenu-title {
      background-color: #f5f5f5;
    }
  }

  :deep(.ant-menu-sub) {
    background: transparent;

    .ant-menu-item {
      padding-left: 48px !important;
    }
  }
}

// 暗色主题
[data-theme='dark'] {
  .layout-menu {
    :deep(.ant-menu-item-selected) {
      background-color: #1890ff;
    }

    :deep(.ant-menu-submenu) {
      > .ant-menu-submenu-title:hover {
        background-color: #262626;
      }

      &.ant-menu-submenu-open > .ant-menu-submenu-title {
        background-color: #262626;
      }
    }
  }
}
</style>