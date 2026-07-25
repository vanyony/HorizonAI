<template>
  <div class="research-page">
    <header class="research-header">
      <div>
        <h2>技术研究助手</h2>
        <p>基于站内情报、GitHub 趋势和你的兴趣生成可验证的技术解读</p>
      </div>
      <el-tag effect="plain">受控工具调用</el-tag>
    </header>

    <div class="research-container">
      <div class="messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="empty">
          <el-icon :size="48"><Search /></el-icon>
          <strong>从一个技术问题开始</strong>
          <span>例如：最近有哪些值得 Java 开发者关注的开源趋势？</span>
        </div>

        <article v-for="(message, index) in messages" :key="message.id || index"
                 class="message" :class="message.role">
          <div class="avatar">
            <el-icon><Cpu v-if="message.role === 'assistant'" /><UserFilled v-else /></el-icon>
          </div>
          <div class="message-main">
            <div class="content">{{ message.content }}</div>

            <div v-if="message.toolInvocations?.length" class="tool-trace">
              <div class="section-title">工具调用</div>
              <div class="tool-list">
                <div v-for="tool in message.toolInvocations" :key="tool.id" class="tool-item">
                  <el-icon><Connection /></el-icon>
                  <span>{{ toolLabel(tool.toolName) }}</span>
                  <el-tag size="small" :type="tool.status === 'SUCCEEDED' ? 'success' : 'danger'">
                    {{ tool.status === 'SUCCEEDED' ? '成功' : '失败' }}
                  </el-tag>
                  <small>{{ tool.durationMs }} ms</small>
                </div>
              </div>
            </div>

            <div v-if="message.evidences?.length" class="evidence-section">
              <div class="section-title">引用来源</div>
              <a v-for="item in message.evidences" :key="item.citationIndex"
                 :href="item.sourceUrl" target="_blank" rel="noreferrer"
                 class="evidence-card">
                <span class="citation">[{{ item.citationIndex }}]</span>
                <span class="evidence-text">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.snippet }}</small>
                </span>
                <el-icon><TopRight /></el-icon>
              </a>
            </div>

            <div v-if="message.traceId" class="trace-id">Trace: {{ message.traceId }}</div>
          </div>
        </article>

        <div v-if="waiting" class="message assistant">
          <div class="avatar"><el-icon><Cpu /></el-icon></div>
          <div class="message-main waiting">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在检索证据并生成解读…
          </div>
        </div>
      </div>

      <div class="composer">
        <el-input v-model="question" type="textarea" :rows="2"
                  placeholder="输入需要研究的技术问题"
                  :disabled="waiting" resize="none"
                  @keyup.enter.exact.prevent="send" />
        <el-button type="primary" :loading="waiting"
                   :disabled="!question.trim()" @click="send">
          <el-icon><Promotion /></el-icon>
          开始研究
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import {
  Connection, Cpu, Loading, Promotion, Search, TopRight, UserFilled
} from '@element-plus/icons-vue'
import {
  askResearch, getResearchMessages, getResearchSessions
} from '@/api/research'

const messages = ref([])
const question = ref('')
const waiting = ref(false)
const sessionId = ref(null)
const messagesRef = ref(null)

const toolNames = {
  site_search: '站内情报检索',
  github_trend: 'GitHub 趋势查询',
  interest_match: '用户兴趣匹配'
}

function toolLabel(name) {
  return toolNames[name] || name
}

async function scrollBottom() {
  await nextTick()
  if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
}

async function send() {
  const value = question.value.trim()
  if (!value || waiting.value) return
  question.value = ''
  messages.value.push({ role: 'user', content: value })
  waiting.value = true
  await scrollBottom()
  try {
    const response = await askResearch(value, sessionId.value)
    const answer = response.data
    sessionId.value = answer.sessionId
    messages.value.push({
      id: answer.messageId,
      role: 'assistant',
      content: answer.answer,
      traceId: answer.traceId,
      evidences: answer.evidences,
      toolInvocations: answer.toolInvocations
    })
    await scrollBottom()
  } finally {
    waiting.value = false
  }
}

onMounted(async () => {
  try {
    const sessions = await getResearchSessions()
    const latest = sessions.data?.[0]
    if (!latest) return
    sessionId.value = latest.id
    const history = await getResearchMessages(latest.id)
    messages.value = (history.data || []).map(message => ({
      ...message,
      evidences: message.citationsJson ? JSON.parse(message.citationsJson) : []
    }))
    await scrollBottom()
  } catch {
    messages.value = []
  }
})
</script>

<style lang="scss" scoped>
.research-page { max-width: 920px; margin: 0 auto; }
.research-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { margin: 0 0 6px; font-size: 22px; }
  p { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
}
.research-container {
  height: calc(100vh - 220px); min-height: 560px; display: flex; flex-direction: column;
  border: 1px solid var(--el-border-color); border-radius: 12px; overflow: hidden;
  background: var(--el-bg-color);
}
.messages { flex: 1; overflow-y: auto; padding: 24px; }
.empty {
  height: 100%; display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 12px; color: var(--el-text-color-secondary);
  .el-icon { color: var(--el-color-primary); }
  span { font-size: 13px; }
}
.message { display: flex; gap: 12px; margin-bottom: 24px; }
.avatar {
  width: 36px; height: 36px; border-radius: 10px; flex-shrink: 0;
  display: grid; place-items: center; background: var(--el-fill-color-light);
}
.assistant .avatar { color: var(--el-color-success); }
.user .avatar { color: var(--el-color-primary); }
.message-main { min-width: 0; flex: 1; }
.content {
  padding: 13px 16px; border-radius: 10px; line-height: 1.75;
  white-space: pre-wrap; word-break: break-word; background: var(--el-fill-color-light);
}
.user .content { background: rgba(0, 188, 212, .08); }
.section-title {
  margin: 12px 0 8px; font-size: 12px; font-weight: 600;
  color: var(--el-text-color-secondary); text-transform: uppercase;
}
.tool-list { display: flex; flex-wrap: wrap; gap: 8px; }
.tool-item {
  display: flex; align-items: center; gap: 6px; padding: 7px 10px;
  border: 1px solid var(--el-border-color); border-radius: 8px; font-size: 12px;
  small { color: var(--el-text-color-placeholder); }
}
.evidence-card {
  display: flex; align-items: flex-start; gap: 10px; padding: 10px 12px;
  margin-bottom: 8px; border: 1px solid var(--el-border-color); border-radius: 8px;
  color: var(--el-text-color-primary); transition: border-color .2s;
  &:hover { border-color: var(--el-color-primary); }
  .citation { color: var(--el-color-primary); font-weight: 600; }
  .evidence-text { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  small { color: var(--el-text-color-secondary); overflow: hidden; text-overflow: ellipsis;
    white-space: nowrap; }
}
.trace-id { margin-top: 8px; font-family: monospace; font-size: 11px;
  color: var(--el-text-color-placeholder); }
.waiting { display: flex; align-items: center; gap: 8px; color: var(--el-text-color-secondary); }
.composer {
  display: flex; gap: 12px; padding: 16px 20px; border-top: 1px solid var(--el-border-color);
  .el-textarea { flex: 1; }
  .el-button { align-self: flex-end; }
}
</style>
