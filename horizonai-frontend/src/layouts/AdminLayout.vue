<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside class="admin-aside">
      <div class="aside-header">
        <router-link to="/" class="admin-logo">
          <span class="logo-icon">◈</span>
          <span class="logo-text">观澜 · 管理</span>
        </router-link>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users/interests">
          <el-icon><DataAnalysis /></el-icon>
          <span>用户兴趣</span>
        </el-menu-item>
        <el-menu-item index="/admin/pipeline">
          <el-icon><Connection /></el-icon>
          <span>内容流水线</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-footer">
        <el-button text @click="$router.push('/')">
          <el-icon><Back /></el-icon>
          返回前台
        </el-button>
      </div>
    </el-aside>

    <!-- 右侧内容 -->
    <div class="admin-right">
      <el-header class="admin-header">
        <span class="page-title">{{ pageTitle }}</span>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document, PriceTag, DataAnalysis, Back, Connection } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const map = {
    '/admin/articles': '内容管理',
    '/admin/tags': '标签管理',
    '/admin/users/interests': '用户兴趣总览',
    '/admin/pipeline': '内容流水线'
  }
  return map[route.path] || '管理中心'
})
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-aside {
  width: 220px;
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.aside-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--el-border-color);

  .admin-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--el-text-color-primary);
    font-size: 16px;
    font-weight: 600;

    .logo-icon {
      color: var(--el-color-primary);
    }
  }
}

.admin-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding-top: 8px;

  .el-menu-item {
    color: var(--el-text-color-regular);
    margin: 2px 8px;
    border-radius: 6px;

    &:hover {
      background: var(--el-fill-color-light);
      color: var(--el-color-primary);
    }

    &.is-active {
      background: rgba(0, 188, 212, 0.15);
      color: var(--el-color-primary);
    }
  }
}

.aside-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color);

  .el-button {
    width: 100%;
    color: var(--el-text-color-secondary);
  }
}

.admin-right {
  margin-left: 220px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  position: sticky;
  top: 0;
  z-index: 50;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }
}

.admin-main {
  flex: 1;
  padding: 24px;
}
</style>
