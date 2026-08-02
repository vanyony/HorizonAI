import { createApp } from 'vue'
import { createPinia } from 'pinia'
import {
  ElAside, ElButton, ElCard, ElCheckbox, ElCheckboxGroup, ElDatePicker,
  ElDialog, ElDropdown, ElDropdownItem, ElDropdownMenu, ElEmpty, ElFooter,
  ElForm, ElFormItem, ElHeader, ElIcon, ElInput, ElLoading, ElMain, ElMenu,
  ElMenuItem, ElOption, ElPagination, ElSelect, ElTable, ElTableColumn,
  ElTag, ElTimeline, ElTimelineItem
} from 'element-plus'
import 'element-plus/es/components/aside/style/css'
import 'element-plus/es/components/button/style/css'
import 'element-plus/es/components/card/style/css'
import 'element-plus/es/components/checkbox/style/css'
import 'element-plus/es/components/checkbox-group/style/css'
import 'element-plus/es/components/date-picker/style/css'
import 'element-plus/es/components/dialog/style/css'
import 'element-plus/es/components/dropdown/style/css'
import 'element-plus/es/components/empty/style/css'
import 'element-plus/es/components/form/style/css'
import 'element-plus/es/components/header/style/css'
import 'element-plus/es/components/icon/style/css'
import 'element-plus/es/components/input/style/css'
import 'element-plus/es/components/loading/style/css'
import 'element-plus/es/components/main/style/css'
import 'element-plus/es/components/menu/style/css'
import 'element-plus/es/components/option/style/css'
import 'element-plus/es/components/pagination/style/css'
import 'element-plus/es/components/select/style/css'
import 'element-plus/es/components/table/style/css'
import 'element-plus/es/components/tag/style/css'
import 'element-plus/es/components/timeline/style/css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import router from './router'
import './styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElLoading)

const components = [
  ElAside, ElButton, ElCard, ElCheckbox, ElCheckboxGroup, ElDatePicker,
  ElDialog, ElDropdown, ElDropdownItem, ElDropdownMenu, ElEmpty, ElFooter,
  ElForm, ElFormItem, ElHeader, ElIcon, ElInput, ElMain, ElMenu, ElMenuItem,
  ElOption, ElPagination, ElSelect, ElTable, ElTableColumn, ElTag, ElTimeline,
  ElTimelineItem
]

for (const component of components) {
  app.component(component.name, component)
}

app.mount('#app')
