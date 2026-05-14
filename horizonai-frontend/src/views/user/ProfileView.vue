<template>
  <div class="profile-page">
    <h2>个人中心</h2>

    <el-card class="section-card" shadow="hover">
      <template #header><span>基本信息</span></template>
      <el-form label-width="80px" v-if="profile">
        <el-form-item label="用户名">
          <span>{{ profile.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="profile.role === 'ADMIN' ? 'danger' : ''" size="small">
            {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="邮箱">
          <template v-if="editingEmail">
            <el-input v-model="emailForm" size="small" style="width: 240px" />
            <el-button type="primary" size="small" @click="saveEmail" style="margin-left: 8px">保存</el-button>
            <el-button size="small" @click="editingEmail = false">取消</el-button>
          </template>
          <template v-else>
            <span>{{ profile.email || '未设置' }}</span>
            <el-button link type="primary" size="small" @click="startEditEmail" style="margin-left: 8px">修改</el-button>
          </template>
        </el-form-item>
      </el-form>
      <el-empty v-else description="加载中..." />
    </el-card>

    <el-card class="section-card" shadow="hover">
      <template #header><span>浏览历史</span></template>
      <div v-if="history.length > 0">
        <div v-for="h in history" :key="h.articleId" class="history-item">
          <router-link :to="`/trends/${h.articleId}`">{{ h.articleTitle }}</router-link>
          <span class="time">{{ formatTime(h.browseTime) }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无浏览记录" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile as getProfileApi, updateProfile, getHistory as getHistoryApi } from '@/api/user'

const profile = ref(null)
const history = ref([])
const editingEmail = ref(false)
const emailForm = ref('')

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

function startEditEmail() {
  emailForm.value = profile.value.email || ''
  editingEmail.value = true
}

async function saveEmail() {
  await updateProfile({ email: emailForm.value })
  profile.value.email = emailForm.value
  editingEmail.value = false
  ElMessage.success('邮箱已更新')
}

onMounted(async () => {
  const [p, h] = await Promise.allSettled([
    getProfileApi(),
    getHistoryApi()
  ])
  if (p.status === 'fulfilled') profile.value = p.value.data
  if (h.status === 'fulfilled') history.value = h.value.data || []
})
</script>

<style lang="scss" scoped>
.profile-page { max-width: 700px; margin: 0 auto; }
h2 { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.section-card { margin-bottom: 20px; background: var(--el-bg-color); border-color: var(--el-border-color); }
.history-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--el-border-color-light);
  a { font-size: 14px; }
  .time { font-size: 12px; color: var(--el-text-color-secondary); white-space: nowrap; margin-left: 16px; }
}
</style>
