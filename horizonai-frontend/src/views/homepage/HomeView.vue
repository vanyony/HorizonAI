<template>
  <div class="home-page">
    <div class="page-hero">
      <h1 class="hero-title">今日技术速览</h1>
      <p class="hero-date">{{ todayDate }}</p>
    </div>

    <!-- 今日 AI 摘要 -->
    <el-card class="section-card digest-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon color="#00BCD4"><MagicStick /></el-icon>
          <span>今日 AI 摘要</span>
        </div>
      </template>
      <el-empty
        description="AI 分析功能将在 Phase 3 接入"
        :image-size="80"
      />
    </el-card>

    <!-- GitHub Trending -->
    <el-card class="section-card trending-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon color="#00BCD4"><TrendCharts /></el-icon>
          <span>GitHub Trending</span>
        </div>
      </template>
      <el-empty
        description="GitHub 趋势数据将在 Phase 3 接入"
        :image-size="80"
      />
    </el-card>

    <!-- 技术新闻 -->
    <el-card class="section-card news-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon color="#00BCD4"><Notebook /></el-icon>
          <span>技术新闻</span>
        </div>
      </template>
      <el-empty
        description="技术新闻将在 Phase 3 接入"
        :image-size="80"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { MagicStick, TrendCharts, Notebook } from '@element-plus/icons-vue'

const todayDate = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const weekMap = ['日', '一', '二', '三', '四', '五', '六']
  const w = weekMap[now.getDay()]
  return `${y}年${m}月${d}日 星期${w}`
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

.section-card {
  margin-bottom: 20px;
  background: var(--el-bg-color);
  border-color: var(--el-border-color);

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
