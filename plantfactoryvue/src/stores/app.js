import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)

  // 主题配置
  const theme = ref('light')
  const primaryColor = ref('#1890ff')

  // 布局配置
  const layout = ref('side') // side, top, mix
  const contentWidth = ref('fluid') // fluid, fixed

  // 面包屑导航
  const breadcrumb = ref(true)

  // 页面加载状态
  const loading = ref(false)

  // 设备类型
  const device = ref('desktop') // desktop, tablet, mobile

  // 当前选中的菜单
  const selectedMenuKeys = ref([])
  const openMenuKeys = ref([])

  // 标签页导航
  const tabs = ref([])
  const activeTabKey = ref('')

  // 全局配置
  const settings = ref({
    showBreadcrumb: true,
    showTabs: true,
    showFooter: true,
    fixedHeader: false,
    fixedSidebar: true,
    colorWeak: false,
    multiTab: true
  })

  // 方法
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed) => {
    sidebarCollapsed.value = collapsed
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    document.body.setAttribute('data-theme', newTheme)
  }

  const setPrimaryColor = (color) => {
    primaryColor.value = color
    document.body.style.setProperty('--primary-color', color)
  }

  const setLayout = (newLayout) => {
    layout.value = newLayout
  }

  const setContentWidth = (width) => {
    contentWidth.value = width
  }

  const setBreadcrumb = (show) => {
    breadcrumb.value = show
    settings.value.showBreadcrumb = show
  }

  const setLoading = (isLoading) => {
    loading.value = isLoading
  }

  const setDevice = (deviceType) => {
    device.value = deviceType
    // 移动端自动折叠侧边栏
    if (deviceType === 'mobile') {
      sidebarCollapsed.value = true
    }
  }

  const setSelectedMenuKeys = (keys) => {
    selectedMenuKeys.value = keys
  }

  const setOpenMenuKeys = (keys) => {
    openMenuKeys.value = keys
  }

  // 标签页相关方法
  const addTab = (tab) => {
    const existTab = tabs.value.find(item => item.key === tab.key)
    if (!existTab) {
      tabs.value.push(tab)
    }
    activeTabKey.value = tab.key
  }

  const removeTab = (targetKey) => {
    const targetIndex = tabs.value.findIndex(item => item.key === targetKey)
    const newTabs = tabs.value.filter(item => item.key !== targetKey)

    if (newTabs.length && targetKey === activeTabKey.value) {
      // 如果删除的是当前激活的标签，则激活相邻的标签
      if (targetIndex === newTabs.length) {
        activeTabKey.value = newTabs[targetIndex - 1].key
      } else {
        activeTabKey.value = newTabs[targetIndex].key
      }
    }

    tabs.value = newTabs
  }

  const closeOtherTabs = (activeKey) => {
    tabs.value = tabs.value.filter(item => item.key === activeKey)
    activeTabKey.value = activeKey
  }

  const closeAllTabs = () => {
    tabs.value = []
    activeTabKey.value = ''
  }

  const setActiveTab = (key) => {
    activeTabKey.value = key
  }

  const updateSettings = (newSettings) => {
    settings.value = { ...settings.value, ...newSettings }
  }

  // 重置所有设置
  const resetSettings = () => {
    sidebarCollapsed.value = false
    theme.value = 'light'
    primaryColor.value = '#1890ff'
    layout.value = 'side'
    contentWidth.value = 'fluid'
    breadcrumb.value = true
    loading.value = false
    selectedMenuKeys.value = []
    openMenuKeys.value = []
    tabs.value = []
    activeTabKey.value = ''

    settings.value = {
      showBreadcrumb: true,
      showTabs: true,
      showFooter: true,
      fixedHeader: false,
      fixedSidebar: true,
      colorWeak: false,
      multiTab: true
    }
  }

  return {
    // 状态
    sidebarCollapsed,
    theme,
    primaryColor,
    layout,
    contentWidth,
    breadcrumb,
    loading,
    device,
    selectedMenuKeys,
    openMenuKeys,
    tabs,
    activeTabKey,
    settings,

    // 方法
    toggleSidebar,
    setSidebarCollapsed,
    setTheme,
    setPrimaryColor,
    setLayout,
    setContentWidth,
    setBreadcrumb,
    setLoading,
    setDevice,
    setSelectedMenuKeys,
    setOpenMenuKeys,
    addTab,
    removeTab,
    closeOtherTabs,
    closeAllTabs,
    setActiveTab,
    updateSettings,
    resetSettings
  }
})