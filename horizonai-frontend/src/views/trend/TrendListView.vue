<template>
  <div class="trend-list-page">
    <div class="page-header">
      <h2>技术趋势</h2>
      <div class="filters">
        <el-select v-model="filter.sourceType" placeholder="来源类型" clearable style="width: 140px" @change="loadData">
          <el-option label="技术新闻" value="NEWS" />
          <el-option label="GitHub项目" value="GITHUB" />
          <el-option label="技术趋势" value="TREND" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无内容" />

      <div class="article-grid">
        <el-card
          v-for="item in tableData"
          :key="item.id"
          class="article-card"
          shadow="hover"
          @click="$router.push(`/trends/${item.id}`)"
        >
          <el-tag :type="sourceTag(item.sourceType)" size="small">{{ sourceLabel(item.sourceType) }}</el-tag>
          <h3 class="title">{{ item.title }}</h3>
          <p class="summary">{{ item.summary || '暂无摘要' }}</p>
          <div class="tags" v-if="item.tags?.length">
            <el-tag v-for="t in item.tags" :key="t.id" size="small" effect="plain">{{ t.name }}</el-tag>
          </div>
        </el-card>
      </div>

      <div class="pagination-wrap" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { getPublicArticles } from '@/api/article'
import { SOURCE_TYPE_LABELS } from '@/utils/constants'

const loading = ref(false)
const tableData = ref([])

const filter = reactive({ sourceType: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

function sourceLabel(type) { return SOURCE_TYPE_LABELS[type] || type }
function sourceTag(type) {
  const map = { NEWS: 'success', GITHUB: '', TREND: 'warning' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPublicArticles({
      page: pagination.page,
      size: pagination.size,
      sourceType: filter.sourceType || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.trend-list-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    font-size: 20px;
    font-weight: 600;
  }
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.article-card {
  cursor: pointer;
  background: var(--el-bg-color);
  border-color: var(--el-border-color);

  &:hover {
    border-color: var(--el-color-primary);
  }

  .title {
    font-size: 16px;
    font-weight: 600;
    margin: 10px 0 8px;
    color: var(--el-text-color-primary);
  }

  .summary {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .tags {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
