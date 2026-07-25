<template>
  <div class="pipeline-page">
    <div class="toolbar">
      <div class="intro">
        <h3>内容流水线</h3>
        <p>查看采集、分析与推荐任务的状态、重试和 Trace 链路</p>
      </div>
      <div class="actions">
        <el-button @click="trigger('DEMO')">导入演示数据</el-button>
        <el-button @click="trigger('RSS')">同步 RSS</el-button>
        <el-button type="primary" @click="trigger('GITHUB')">同步 GitHub</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <div class="filters">
        <el-select v-model="filters.status" clearable placeholder="任务状态" @change="load">
          <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
        </el-select>
        <el-select v-model="filters.taskType" clearable placeholder="任务类型" @change="load">
          <el-option v-for="type in taskTypes" :key="type" :label="type" :value="type" />
        </el-select>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="taskType" label="任务类型" min-width="170" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行次数" width="100">
          <template #default="{ row }">{{ row.attempt }} / {{ row.maxAttempts }}</template>
        </el-table-column>
        <el-table-column prop="bizKey" label="业务标识" min-width="130" show-overflow-tooltip />
        <el-table-column prop="traceId" label="Trace" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" @click="showTrace(row.traceId)">
              {{ row.traceId }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="最后错误" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'FAILED'" link type="warning" @click="retry(row)">
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="filters.page" v-model:page-size="filters.size"
                     :total="total" layout="total, prev, pager, next" @current-change="load" />
    </el-card>

    <el-dialog v-model="traceVisible" title="Trace 任务链路" width="760px">
      <el-timeline>
        <el-timeline-item v-for="item in traceTasks" :key="item.id"
                          :timestamp="item.createdAt" placement="top">
          <el-card shadow="never">
            <div class="trace-item">
              <strong>{{ item.taskType }}</strong>
              <el-tag :type="statusType(item.status)" size="small">{{ item.status }}</el-tag>
              <span>执行 {{ item.attempt }} 次</span>
            </div>
            <p v-if="item.errorMessage">{{ item.errorMessage }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPipelineTasks, getTraceTasks, retryTask, syncSource } from '@/api/pipeline'

const loading = ref(false)
const tasks = ref([])
const total = ref(0)
const traceVisible = ref(false)
const traceTasks = ref([])
const statuses = ['PENDING', 'RUNNING', 'RETRY_WAIT', 'SUCCEEDED', 'FAILED']
const taskTypes = ['COLLECT_GITHUB', 'COLLECT_RSS', 'COLLECT_DEMO',
  'ANALYZE_ARTICLE', 'REFRESH_RECOMMENDATION']
const filters = reactive({ page: 1, size: 20, status: '', taskType: '' })

function statusType(status) {
  return { SUCCEEDED: 'success', FAILED: 'danger', RETRY_WAIT: 'warning', RUNNING: 'primary' }[status] || 'info'
}

async function load() {
  loading.value = true
  try {
    const response = await getPipelineTasks(filters)
    tasks.value = response.data.records || []
    total.value = response.data.total || 0
  } finally {
    loading.value = false
  }
}

async function trigger(source) {
  await syncSource(source)
  ElMessage.success(`${source} 同步任务已提交`)
  await load()
}

async function retry(row) {
  await retryTask(row.id)
  ElMessage.success('任务已重新进入调度队列')
  await load()
}

async function showTrace(traceId) {
  const response = await getTraceTasks(traceId)
  traceTasks.value = response.data || []
  traceVisible.value = true
}

onMounted(load)
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.intro {
  h3 { margin: 0 0 6px; }
  p { margin: 0; font-size: 13px; color: var(--el-text-color-secondary); }
}
.actions, .filters { display: flex; gap: 10px; }
.filters { margin-bottom: 16px; }
.el-pagination { justify-content: flex-end; margin-top: 18px; }
.trace-item { display: flex; align-items: center; gap: 12px;
  span { color: var(--el-text-color-secondary); font-size: 12px; } }
</style>
