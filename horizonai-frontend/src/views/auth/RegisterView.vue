<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <span class="logo-icon">◈</span>
        <h1 class="app-title">创建账号</h1>
        <p class="app-desc">加入观澜，获取每日技术趋势洞察</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="3-20 个字符"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6-30 个字符"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item label="邮箱（选填）" prop="email">
          <el-input
            v-model="form.email"
            placeholder="example@mail.com"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <el-form-item label="技术兴趣（可多选）">
          <el-checkbox-group v-model="form.interestTagIds" class="interest-group">
            <el-checkbox v-for="t in tagList" :key="t.id" :label="t.id" border>
              {{ t.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { getTags } from '@/api/tag'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const tagList = ref([])

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  interestTagIds: []
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度 6-30 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  const res = await getTags()
  tagList.value = res.data || []
})

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      email: form.email || undefined,
      interestTagIds: form.interestTagIds
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 错误已由 request 拦截器统一提示
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 156px);
  padding: 24px 0;
}

.register-card {
  width: 460px;
  padding: 40px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 188, 212, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 28px;

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

.register-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.interest-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .el-checkbox {
    margin-right: 0;
  }
}

.register-btn {
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
