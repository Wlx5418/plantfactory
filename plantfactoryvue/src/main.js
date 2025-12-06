import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

// 样式导入
import 'ant-design-vue/dist/reset.css'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

// 初始化用户权限
const userStore = useUserStore()
userStore.initAuth()

app.mount('#app')