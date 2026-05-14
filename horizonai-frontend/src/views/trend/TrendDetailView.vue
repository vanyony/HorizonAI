<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="article">
      <div class="back-link">
        <router-link to="/trends">
          <el-icon><ArrowLeft /></el-icon>返回趋势列表
        </router-link>
      </div>

      <div class="article-header">
        <el-tag :type="sourceTag(article.sourceType)" size="small">
          {{ sourceLabel(article.sourceType) }}
        </el-tag>
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <span v-if="article.publishDate">发布于 {{ article.publishDate }}</span>
          <span v-if="article.sourceUrl">
            <a :href="article.sourceUrl" target="_blank">查看原文 <el-icon><Link /></el-icon></a>
          </span>
        </div>
        <div class="tags" v-if="article.tags?.length">
          <el-tag v-for="t in article.tags" :key="t.id" size="small" effect="plain">
            {{ t.name }}
          </el-tag>
        </div>
      </div>

      <div class="article-content" v-if="article.content">
        <p>{{ article.content }}</p>
      </div>

      <!-- AI 分析面板 -->
      <el-card v-if="analysis" class="analysis-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <el-icon color="#00BCD4"><MagicStick /></el-icon>
            <span>AI 深度分析</span>
            <el-tag v-if="analysis.importanceRating" type="warning" size="small">
              重要度 {{ analysis.importanceRating }}/10
            </el-tag>
          </div>
        </template>

        <div class="analysis-grid">
          <div class="analysis-block">
            <h4>目标受众</h4>
            <p>{{ analysis.targetAudience || '暂无分析' }}</p>
          </div>
          <div class="analysis-block">
            <h4>行业影响</h4>
            <p>{{ analysis.industryImpact || '暂无分析' }}</p>
          </div>
          <div class="analysis-block full-width">
            <h4>学习建议</h4>
            <p>{{ analysis.learningSuggestions || '暂无建议' }}</p>
          </div>
        </div>
      </el-card>

      <el-empty v-else description="该内容尚未进行 AI 分析" :image-size="60" />
    </template>

    <el-empty v-else-if="!loading" description="内容不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, MagicStick, Link } from '@element-plus/icons-vue'
import { getPublicArticle } from '@/api/article'
import { SOURCE_TYPE_LABELS } from '@/utils/constants'
import request from '@/utils/request'

const route = useRoute()
const loading = ref(false)
const article = ref(null)
const analysis = ref(null)

function sourceLabel(type) { return SOURCE_TYPE_LABELS[type] || type }
function sourceTag(type) {
  const map = { NEWS: 'success', GITHUB: '', TREND: 'warning' }
  return map[type] || 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    const id = route.params.id
    const [articleRes, analysisRes] = await Promise.allSettled([
      getPublicArticle(id),
      request.get(`/articles/${id}/analysis`)
    ])
    if (articleRes.status === 'fulfilled') {
      article.value = articleRes.value.data
    }
    if (analysisRes.status === 'fulfilled' && analysisRes.value.data) {
      analysis.value = analysisRes.value.data
    }
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.detail-page {
  max-width: 800px;
  margin: 0 auto;
}

.back-link {
  margin-bottom: 16px;
  a {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: var(--el-text-color-secondary);

    &:hover { color: var(--el-color-primary); }
  }
}

.article-header {
  margin-bottom: 24px;

  .title {
    font-size: 24px;
    font-weight: 700;
    margin: 12px 0 8px;
    line-height: 1.4;
  }

  .meta {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-bottom: 10px;

    a {
      display: inline-flex;
      align-items: center;
      gap: 2px;
    }
  }

  .tags { display: flex; gap: 6px; flex-wrap: wrap; }
}

.article-content {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  font-size: 15px;
  line-height: 1.8;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
}

.analysis-panel {
  background: var(--el-bg-color);
  border-color: var(--el-border-color);

  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;

    .el-tag { margin-left: auto; }
  }
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  .full-width { grid-column: 1 / -1; }

  .analysis-block {
    h4 {
      font-size: 14px;
      color: var(--el-color-primary);
      margin-bottom: 6px;
    }

    p {
      font-size: 14px;
      color: var(--el-text-color-regular);
      line-height: 1.7;
      white-space: pre-wrap;
    }
  }
}
</style>
