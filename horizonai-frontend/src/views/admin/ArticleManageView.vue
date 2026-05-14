<template>
  <div class="article-manage">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select v-model="filter.sourceType" placeholder="来源类型" clearable style="width: 140px" @change="loadData">
          <el-option label="技术新闻" value="NEWS" />
          <el-option label="GitHub项目" value="GITHUB" />
          <el-option label="技术趋势" value="TREND" />
        </el-select>
        <el-select v-model="filter.tagId" placeholder="标签筛选" clearable style="width: 140px; margin-left: 8px" @change="loadData">
          <el-option v-for="t in tagList" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新增内容
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="sourceTypeTag(row.sourceType)" size="small">
            {{ sourceTypeLabel(row.sourceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="200">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag.id" size="small" style="margin-right: 4px">
            {{ tag.name }}
          </el-tag>
          <span v-if="!row.tags?.length" style="color: var(--el-text-color-secondary)">无</span>
        </template>
      </el-table-column>
      <el-table-column label="重要度" width="90">
        <template #default="{ row }">
          <span v-if="row.importanceRating">{{ row.importanceRating }}</span>
          <span v-else style="color: var(--el-text-color-secondary)">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="publishDate" label="发布日期" width="120" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑内容' : '新增内容'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="dialog.form" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="dialog.form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="来源类型" prop="sourceType">
          <el-select v-model="dialog.form.sourceType" placeholder="请选择" style="width: 100%">
            <el-option label="技术新闻" value="NEWS" />
            <el-option label="GitHub项目" value="GITHUB" />
            <el-option label="技术趋势" value="TREND" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-checkbox-group v-model="dialog.form.tagIds">
            <el-checkbox v-for="t in tagList" :key="t.id" :label="t.id" :value="t.id">
              {{ t.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="来源链接">
          <el-input v-model="dialog.form.sourceUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="dialog.form.publishDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="dialog.form.summary" type="textarea" :rows="3" placeholder="简短摘要（可选）" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dialog.form.content" type="textarea" :rows="6" placeholder="文章/项目的详细描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getArticles, createArticle, updateArticle, deleteArticle } from '@/api/admin'
import { getTags } from '@/api/tag'

const loading = ref(false)
const tableData = ref([])
const tagList = ref([])
const formRef = ref(null)

const filter = reactive({
  sourceType: '',
  tagId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialog = reactive({
  visible: false,
  isEdit: false,
  submitting: false,
  editId: null,
  form: {
    title: '',
    sourceType: '',
    tagIds: [],
    sourceUrl: '',
    publishDate: null,
    summary: '',
    content: ''
  }
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }]
}

function sourceTypeLabel(type) {
  const map = { NEWS: '技术新闻', GITHUB: 'GitHub项目', TREND: '技术趋势' }
  return map[type] || type
}

function sourceTypeTag(type) {
  const map = { NEWS: 'success', GITHUB: '', TREND: 'warning' }
  return map[type] || 'info'
}

async function loadTags() {
  const res = await getTags()
  tagList.value = res.data || []
}

async function loadData() {
  loading.value = true
  try {
    const res = await getArticles({
      page: pagination.page,
      size: pagination.size,
      sourceType: filter.sourceType || undefined,
      tagId: filter.tagId || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  dialog.form = {
    title: '',
    sourceType: '',
    tagIds: [],
    sourceUrl: '',
    publishDate: null,
    summary: '',
    content: ''
  }
}

function openCreate() {
  dialog.isEdit = false
  dialog.editId = null
  resetForm()
  dialog.visible = true
}

function openEdit(row) {
  dialog.isEdit = true
  dialog.editId = row.id
  dialog.form = {
    title: row.title,
    sourceType: row.sourceType,
    tagIds: row.tags?.map(t => t.id) || [],
    sourceUrl: row.sourceUrl || '',
    publishDate: row.publishDate || null,
    summary: row.summary || '',
    content: row.content || ''
  }
  dialog.visible = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  dialog.submitting = true
  try {
    const data = {
      ...dialog.form,
      publishDate: dialog.form.publishDate
        ? new Date(dialog.form.publishDate).toISOString().split('T')[0]
        : null
    }
    if (dialog.isEdit) {
      await updateArticle(dialog.editId, data)
      ElMessage.success('更新成功')
    } else {
      await createArticle(data)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    loadData()
  } finally {
    dialog.submitting = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除'
  })
  await deleteArticle(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => {
  loadTags()
  loadData()
})
</script>

<style lang="scss" scoped>
.article-manage {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
