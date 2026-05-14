<template>
  <div class="digest-page">
    <h2>每日摘要归档</h2>

    <div v-loading="loading">
      <el-empty v-if="!loading && digests.length === 0" description="暂无摘要，请在后台触发 AI 分析后自动生成" />

      <div class="digest-list">
        <el-card v-for="d in digests" :key="d.id" class="digest-card" shadow="hover">
          <div class="digest-date">{{ d.digestDate }}</div>
          <h3 class="digest-title">{{ d.title }}</h3>
          <p class="digest-summary">{{ d.summary }}</p>
        </el-card>
      </div>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDigests } from '@/api/digest'

const loading = ref(false)
const digests = ref([])
const currentPage = ref(1)
const total = ref(0)

async function loadData() {
  loading.value = true
  try {
    const res = await getDigests({ page: currentPage.value, size: 10 })
    digests.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.digest-page { max-width: 800px; margin: 0 auto; }
h2 { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.digest-list { display: flex; flex-direction: column; gap: 16px; }
.digest-card { background: var(--el-bg-color); border-color: var(--el-border-color); }
.digest-date { font-size: 12px; color: var(--el-color-primary); margin-bottom: 4px; }
.digest-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.digest-summary { font-size: 14px; color: var(--el-text-color-secondary); line-height: 1.7; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
