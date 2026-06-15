import { createApp } from 'vue'
import App from './App.vue'
import './assets/main.css'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
const app = createApp(App)

app.use(router)
app.use(ElementPlus, { locale: zhCn, dialog: { appendToBody: true }, messagebox: { appendToBody: true } })
app.mount('#app')
