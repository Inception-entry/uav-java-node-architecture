import { createApp } from 'vue'
import App from './App.vue'
import { Button, ConfigProvider, Dropdown, Menu, Drawer, message, Tabs } from 'ant-design-vue';
import { store } from './store';
import router from './router'
import register from './components/pe-global-register'
import i18next from "i18next"
import I18NextVue from "i18next-vue"

// global css
import '@/style/common.scss'
import '@/style/reset.scss'

// cesium vue
import cesiumVue from '@/libs/cesium/cesium-vue'

const app = createApp(App)

app.use(store).use(router).use(I18NextVue, { i18next }).use(cesiumVue)

// 引入antd的组件
app.use(Button)
app.use(ConfigProvider)
app.use(Dropdown)
app.use(Menu)
app.use(Drawer)
app.use(Tabs)

app.config.globalProperties.$message = message;

register(app)

app.mount('#app')
