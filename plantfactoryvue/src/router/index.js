import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 布局组件
const Layout = () => import('../layout/index.vue')

// 登录页面
const Login = () => import('../views/login/index.vue')

// 主要页面
const Dashboard = () => import('../views/dashboard/index.vue')
const EnvironmentMonitor = () => import('../views/environment/monitor.vue')
const SensorData = () => import('../views/environment/sensor-data.vue')
const HistoricalData = () => import('../views/environment/historical-data.vue')
const UserManagement = () => import('../views/user/index.vue')
const RoleManagement = () => import('../views/system/roles.vue')
const ProductionArea = () => import('../views/production/areas.vue')
const PartitionManagement = () => import('../views/production/devices.vue')
const DataAnalysis = () => import('../views/data/analysis.vue')
const Settings = () => import('../views/settings/index.vue')
const Profile = () => import('../views/system/profile.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '系统概览',
          icon: 'dashboard',
          requiresAuth: true
        }
      },
      {
        path: 'environment',
        name: 'Environment',
        redirect: '/environment/monitor',
        meta: {
          title: '环境监控',
          icon: 'monitor',
          requiresAuth: true
        },
        children: [
          {
            path: 'monitor',
            name: 'EnvironmentMonitor',
            component: EnvironmentMonitor,
            meta: {
              title: '实时监控',
              requiresAuth: true
            }
          },
          {
            path: 'sensor-data',
            name: 'SensorData',
            component: SensorData,
            meta: {
              title: '传感器数据',
              requiresAuth: true
            }
          },
          {
            path: 'historical-data',
            name: 'HistoricalData',
            component: HistoricalData,
            meta: {
              title: '历史数据',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'user',
        name: 'User',
        redirect: '/user/management',
        meta: {
          title: '用户管理',
          icon: 'user',
          requiresAuth: true,
          roles: ['ADMIN']
        },
        children: [
          {
            path: 'management',
            name: 'UserManagement',
            component: UserManagement,
            meta: {
              title: '用户列表',
              requiresAuth: true,
              roles: ['ADMIN']
            }
          },
          {
            path: 'roles',
            name: 'RoleManagement',
            component: RoleManagement,
            meta: {
              title: '角色管理',
              requiresAuth: true,
              roles: ['ADMIN']
            }
          },
          {
            path: 'profile',
            name: 'Profile',
            component: Profile,
            meta: {
              title: '个人资料',
              requiresAuth: true,
              hideInMenu: true
            }
          }
        ]
      },
      {
        path: 'production',
        name: 'Production',
        redirect: '/production/area',
        meta: {
          title: '生产管理',
          icon: 'apartment',
          requiresAuth: true
        },
        children: [
          {
            path: 'area',
            name: 'ProductionArea',
            component: ProductionArea,
            meta: {
              title: '生产区域',
              requiresAuth: true
            }
          },
          {
            path: 'partition',
            name: 'PartitionManagement',
            component: PartitionManagement,
            meta: {
              title: '分区管理',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'analysis',
        name: 'DataAnalysis',
        component: DataAnalysis,
        meta: {
          title: '数据分析',
          icon: 'bar-chart',
          requiresAuth: true
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings,
        meta: {
          title: '系统设置',
          icon: 'setting',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: {
      title: '404',
      hideInMenu: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  const userPermissions = userStore.permissions

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 植物工厂管理系统` : '植物工厂管理系统'

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }

    // 检查角色权限
    if (to.meta.roles && !to.meta.roles.some(role => userPermissions.includes(role))) {
      // 可以跳转到无权限页面或显示提示
      next('/dashboard')
      return
    }
  }

  // 如果已登录且访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  next()
})

export default router