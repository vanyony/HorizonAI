<template>
  <div class="tag-page">
    <div class="back-link">
      <router-link to="/trends"><el-icon><ArrowLeft /></el-icon>返回趋势列表</router-link>
    </div>
    <h2>标签：{{ $route.params.tag }}</h2>

    <div v-loading="loading">
      <el-empty v-if="!loading && articles.length === 0" description="该标签下暂无内容" />

      <div class="article-grid">
        <el-card v-for="item in articles" :key="item.id" class="article-card" shadow="hover"
                 @click="$router.push(`/trends/${item.id}`)">
          <el-tag :type="sourceTag(item.sourceType)" size="small">{{ sourceLabel(item.sourceType) }}</el-tag>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary || '暂无摘要' }}</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPublicArticles } from '@/api/article'
import { SOURCE_TYPE_LABELS } from '@/utils/constants'

const route = useRoute()
const loading = ref(false)
const articles = ref([])

function sourceLabel(type) { return SOURCE_TYPE_LABELS[type] || type }
function sourceTag(type) {
  const map = { NEWS: 'success', GITHUB: '', TREND: 'warning' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const tagName = route.params.tag
    const res = await getPublicArticles({ page: 1, size: 20 })
    // 前端按标签名过滤（简化处理）
    articles.value = (res.data?.records || []).filter(a =>
      a.tags?.some(t => t.name === tagName)
    )
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
watch(() => route.params.tag, loadData)
</script>

<style lang="scss" scoped>
.tag-page { max-width: 900px; margin: 0 auto; }
.back-link { margin-bottom: 12px; a { font-size: 14px; color: var(--el-text-color-secondary); &:hover { color: var(--el-color-primary); } } }
h2 { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.article-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.article-card { cursor: pointer; background: var(--el-bg-color); border-color: var(--el-border-color);
  &:hover { border-color: var(--el-color-primary); }
  h3 { font-size: 16px; margin: 10px 0 8px; }
  p { font-size: 13px; color: var(--el-text-color-secondary); display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
}
</style>
