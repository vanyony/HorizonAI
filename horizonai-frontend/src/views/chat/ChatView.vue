<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 对话区域 -->
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-icon :size="48" color="#00BCD4"><ChatDotRound /></el-icon>
          <p>我是观澜 AI，你的技术趋势顾问</p>
          <p class="hint">可以问我技术趋势、编程学习、技术选型相关的问题</p>
        </div>

        <div
          v-for="msg in messages"
          :key="msg.id || Math.random()"
          class="message"
          :class="msg.role"
        >
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'assistant'" :size="24"><Cpu /></el-icon>
            <el-icon v-else :size="24"><UserFilled /></el-icon>
          </div>
          <div class="message-body">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time" v-if="msg.createdAt">
              {{ formatTime(msg.createdAt) }}
            </div>
          </div>
        </div>

        <div v-if="waiting" class="message assistant">
          <div class="message-avatar">
            <el-icon :size="24"><Cpu /></el-icon>
          </div>
          <div class="message-body">
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入技术问题，例如：最近前端有什么值得关注的技术趋势？"
          :disabled="waiting"
          @keyup.enter.exact="handleSend"
          resize="none"
        />
        <el-button
          type="primary"
          :disabled="!inputText.trim() || waiting"
          :loading="waiting"
          @click="handleSend"
        >
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ChatDotRound, Cpu, UserFilled, Promotion } from '@element-plus/icons-vue'
import { sendMessage, getHistory } from '@/api/chat'

const messages = ref([])
const inputText = ref('')
const waiting = ref(false)
const messagesRef = ref(null)

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function scrollToBottom() {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

async function handleSend() {
  const content = inputText.value.trim()
  if (!content || waiting.value) return

  inputText.value = ''
  messages.value.push({ role: 'user', content, createdAt: new Date().toISOString() })
  await scrollToBottom()

  waiting.value = true
  try {
    const res = await sendMessage(content)
    messages.value.push(res.data)
    await scrollToBottom()
  } finally {
    waiting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getHistory()
    messages.value = res.data || []
    await scrollToBottom()
  } catch {
    // 未登录或加载失败
  }
})
</script>

<style lang="scss" scoped>
.chat-page {
  max-width: 720px;
  margin: 0 auto;
  height: calc(100vh - 180px);
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-secondary);

  p {
    margin-top: 12px;
    font-size: 15px;
  }

  .hint {
    font-size: 13px;
    color: var(--el-text-color-placeholder);
  }
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .message-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }

  &.user .message-avatar {
    background: rgba(0, 188, 212, 0.15);
    color: var(--el-color-primary);
  }

  &.assistant .message-avatar {
    background: rgba(103, 194, 58, 0.15);
    color: var(--el-color-success);
  }

  .message-body {
    flex: 1;
    min-width: 0;
  }

  .message-content {
    background: var(--el-fill-color-light);
    padding: 12px 16px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.7;
    color: var(--el-text-color-regular);
    white-space: pre-wrap;
    word-break: break-word;
  }

  &.user .message-content {
    background: rgba(0, 188, 212, 0.08);
  }

  .message-time {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    margin-top: 4px;
    padding-left: 4px;
  }
}

.typing-dots {
  display: flex;
  gap: 4px;
  padding: 16px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--el-color-primary);
    animation: bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color);
  background: var(--el-bg-color);

  .el-textarea {
    flex: 1;
  }

  .el-button {
    align-self: flex-end;
  }
}
</style>
