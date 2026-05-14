<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-header">
        <span class="logo-icon">◈</span>
        <h1 class="app-title">观澜 HorizonAI</h1>
        <p class="app-desc">AI 驱动的技术情报平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度 6-30 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    const { token, username, role } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('username', username)
    localStorage.setItem('role', role)
    ElMessage.success(`欢迎回来，${username}`)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch {
    // 错误已由 request 拦截器统一提示
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 156px);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 188, 212, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    font-size: 36px;
    color: var(--el-color-primary);
  }

  .app-title {
    font-size: 22px;
    font-weight: 600;
    margin: 8px 0 4px;
    color: var(--el-text-color-primary);
  }

  .app-desc {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 4px;
}

.card-footer {
  text-align: center;
  font-size: 14px;
  color: var(--el-text-color-secondary);

  a {
    margin-left: 4px;
  }
}
</style>
