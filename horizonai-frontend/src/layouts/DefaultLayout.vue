<template>
  <div class="default-layout">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">◈</span>
          <span class="logo-text">观澜 HorizonAI</span>
        </router-link>
      </div>
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          router
          class="main-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/trends">技术趋势</el-menu-item>
          <el-menu-item index="/chat">AI 对话</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="isLoggedIn">
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <el-icon><UserFilled /></el-icon>
              <span class="username">{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <router-link to="/profile">个人中心</router-link>
                </el-dropdown-item>
                <el-dropdown-item command="interests">
                  <router-link to="/interests">兴趣管理</router-link>
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin" divided>
                  <router-link to="/admin/articles">后台管理</router-link>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/login')">
            登录
          </el-button>
          <el-button size="small" @click="$router.push('/register')">
            注册
          </el-button>
        </template>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-main class="layout-main">
      <router-view />
    </el-main>

    <!-- 底部栏 -->
    <el-footer class="layout-footer">
      <span>HorizonAI 观澜 — AI 驱动的技术情报平台</span>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const username = computed(() => localStorage.getItem('username') || '用户')
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/trends') || path.startsWith('/tags')) return '/trends'
  if (path.startsWith('/chat')) return '/chat'
  return '/'
})

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--el-text-color-primary);
    font-size: 18px;
    font-weight: 600;

    .logo-icon {
      color: var(--el-color-primary);
      font-size: 22px;
    }
  }
}

.header-center {
  .main-menu {
    background: transparent;
    border-bottom: none;

    .el-menu-item {
      color: var(--el-text-color-regular);
      border-bottom-color: transparent;

      &:hover {
        background: var(--el-fill-color-light);
        color: var(--el-color-primary);
      }

      &.is-active {
        color: var(--el-color-primary);
        border-bottom-color: var(--el-color-primary);
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;

  .user-dropdown {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 6px;
    color: var(--el-text-color-regular);

    &:hover {
      background: var(--el-fill-color-light);
    }
  }
}

.layout-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.layout-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  border-top: 1px solid var(--el-border-color);
  background-color: var(--el-bg-color);
}
</style>
