<template>
  <div class="interest-page">
    <h2>兴趣管理</h2>
    <p class="desc">选择你关注的技术领域，我们将为你推荐相关内容</p>

    <el-card class="section-card" shadow="hover">
      <el-checkbox-group v-model="selectedIds" class="tag-grid">
        <el-checkbox v-for="t in allTags" :key="t.id" :label="t.id" :value="t.id" border>
          {{ t.name }}
        </el-checkbox>
      </el-checkbox-group>

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="save">保存兴趣</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTags } from '@/api/tag'
import { getInterests, updateInterests } from '@/api/user'

const allTags = ref([])
const selectedIds = ref([])
const saving = ref(false)

onMounted(async () => {
  const [tagsRes, interestsRes] = await Promise.allSettled([
    getTags(),
    getInterests()
  ])
  if (tagsRes.status === 'fulfilled') allTags.value = tagsRes.value.data || []
  if (interestsRes.status === 'fulfilled') {
    selectedIds.value = (interestsRes.value.data || []).map(t => t.id)
  }
})

async function save() {
  saving.value = true
  try {
    await updateInterests(selectedIds.value)
    ElMessage.success('兴趣已保存')
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.interest-page { max-width: 700px; margin: 0 auto; }
h2 { font-size: 20px; font-weight: 600; margin-bottom: 8px; }
.desc { font-size: 14px; color: var(--el-text-color-secondary); margin-bottom: 20px; }
.section-card { background: var(--el-bg-color); border-color: var(--el-border-color); }
.tag-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.actions { margin-top: 24px; }
</style>
