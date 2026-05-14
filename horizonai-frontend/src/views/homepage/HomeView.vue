<template>
  <div class="home-page">
    <div class="page-hero">
      <h1 class="hero-title">今日技术速览</h1>
      <p class="hero-date">{{ todayDate }}</p>
    </div>

    <el-empty v-if="!loading && items.length === 0"
      description="暂无内容，请先在后台上传文章并触发 AI 分析"
      :image-size="80" />

    <!-- 趋势卡片列表 -->
    <div v-loading="loading" class="trend-list">
      <el-card
        v-for="item in items"
        :key="item.id"
        class="trend-card"
        shadow="hover"
        @click="$router.push(`/trends/${item.id}`)"
      >
        <div class="card-body">
          <div class="card-main">
            <div class="card-header-row">
              <el-tag :type="sourceTag(item.sourceType)" size="small">
                {{ sourceLabel(item.sourceType) }}
              </el-tag>
              <span v-if="item.importanceRating" class="rating-badge">
                <el-icon><StarFilled /></el-icon>
                {{ item.importanceRating }}
              </span>
            </div>
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-summary">{{ item.summary || item.analysis?.summary || '暂无 AI 摘要' }}</p>
          </div>
          <div v-if="item.analysis" class="card-analysis">
            <div class="analysis-item">
              <span class="label">目标受众</span>
              <span class="value">{{ item.analysis.targetAudience }}</span>
            </div>
            <div class="analysis-item">
              <span class="label">行业影响</span>
              <span class="value">{{ truncate(item.analysis.industryImpact, 60) }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'
import { getTodayOverview } from '@/api/homepage'
import { SOURCE_TYPE_LABELS } from '@/utils/constants'

const loading = ref(false)
const items = ref([])

const todayDate = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const weekMap = ['日', '一', '二', '三', '四', '五', '六']
  return `${y}年${m}月${d}日 星期${weekMap[now.getDay()]}`
})

function sourceLabel(type) { return SOURCE_TYPE_LABELS[type] || type }
function sourceTag(type) {
  const map = { NEWS: 'success', GITHUB: '', TREND: 'warning' }
  return map[type] || 'info'
}
function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '...' : text
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getTodayOverview()
    items.value = res.data?.items || []
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-hero {
  text-align: center;
  margin-bottom: 32px;

  .hero-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
  }

  .hero-date {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trend-card {
  cursor: pointer;
  background: var(--el-bg-color);
  border-color: var(--el-border-color);
  transition: border-color 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.card-body {
  display: flex;
  gap: 24px;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  .rating-badge {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    color: #E6A23C;
    font-weight: 600;
  }
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}

.card-summary {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.card-analysis {
  width: 200px;
  flex-shrink: 0;
  padding-left: 20px;
  border-left: 1px solid var(--el-border-color-light);

  .analysis-item {
    margin-bottom: 12px;

    .label {
      display: block;
      font-size: 12px;
      color: var(--el-color-primary);
      margin-bottom: 2px;
    }

    .value {
      font-size: 13px;
      color: var(--el-text-color-regular);
      line-height: 1.5;
    }
  }
}

@media (max-width: 768px) {
  .card-body {
    flex-direction: column;
  }
  .card-analysis {
    width: 100%;
    padding-left: 0;
    padding-top: 12px;
    border-left: none;
    border-top: 1px solid var(--el-border-color-light);
  }
}
</style>
