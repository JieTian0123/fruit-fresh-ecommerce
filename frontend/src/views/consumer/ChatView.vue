<template>
  <div class="chat-page">
    <div class="chat-header">
      <div>
        <h2>{{ currentMerchantName }}</h2>
        <p>客服聊天页已独立展示，当前使用站内模拟消息。</p>
      </div>
      <el-button link type="primary" @click="router.push('/messages')">返回消息中心</el-button>
    </div>

    <div class="chat-panel">
      <div ref="messageListRef" class="message-list">
        <div v-if="chatMessages.length === 0" class="empty-wrap">
          <el-empty description="暂无聊天记录，先和商家打个招呼吧" />
        </div>
        <div
          v-for="msg in chatMessages"
          :key="msg.id"
          class="message-item"
          :class="{ self: msg.senderId === currentUserId }"
        >
          <el-avatar v-if="msg.senderId !== currentUserId" :size="36" :src="msg.senderAvatar">
            {{ msg.senderName?.[0] || '商' }}
          </el-avatar>

          <div class="message-bubble">
            <div v-if="msg.senderId !== currentUserId" class="sender-name">{{ msg.senderName }}</div>
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatDateTime(msg.createTime) }}</div>
          </div>

          <el-avatar v-if="msg.senderId === currentUserId" :size="36">我</el-avatar>
        </div>
      </div>

      <div class="message-input">
        <el-input
          v-model="messageInput"
          type="textarea"
          :rows="3"
          placeholder="输入消息，按 Enter 发送"
          @keydown.enter.exact.prevent="handleSendMessage"
        />
        <div class="input-actions">
          <span class="tip">聊天功能当前为演示版，消息仅本地模拟。</span>
          <el-button type="primary" :loading="sending" @click="handleSendMessage">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Conversation, Message } from '@/types'
import { getChatMessages, getConversations, sendMessage } from '@/api/message'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const currentUserId = 1
const messageInput = ref('')
const sending = ref(false)
const messageListRef = ref<HTMLElement>()
const chatMessages = ref<Message[]>([])
const currentMerchantName = ref('商家客服')

async function loadConversationMeta(merchantId: number) {
  const conversations: Conversation[] = await getConversations()
  const target = conversations.find(item => item.merchantId === merchantId)
  currentMerchantName.value = target?.shopName || target?.merchantName || `商家${merchantId}`
}

async function loadMessages() {
  const merchantId = Number(route.params.merchantId)
  if (!merchantId) {
    router.push('/messages')
    return
  }

  await loadConversationMeta(merchantId)
  chatMessages.value = await getChatMessages(merchantId)
  await nextTick()
  scrollToBottom()
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSendMessage() {
  const merchantId = Number(route.params.merchantId)
  if (!merchantId || !messageInput.value.trim() || sending.value) {
    return
  }

  sending.value = true
  try {
    const newMessage = await sendMessage(merchantId, messageInput.value.trim())
    chatMessages.value.push(newMessage)
    messageInput.value = ''
    await nextTick()
    scrollToBottom()
  } finally {
    sending.value = false
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.chat-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.chat-header h2 {
  margin: 0 0 6px;
}

.chat-header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.chat-panel {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  min-height: 70vh;
}

.message-list {
  flex: 1;
  min-height: 480px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, #f8fbf6 0%, #ffffff 100%);
}

.empty-wrap {
  margin: auto 0;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: min(70%, 520px);
  padding: 12px 16px;
  border-radius: 14px;
  background: #f2f4f7;
}

.message-item.self .message-bubble {
  background: var(--color-primary);
  color: #fff;
}

.sender-name {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.message-content {
  line-height: 1.6;
  word-break: break-word;
}

.message-time {
  margin-top: 6px;
  font-size: 11px;
  color: var(--color-text-secondary);
  text-align: right;
}

.message-item.self .message-time {
  color: rgba(255, 255, 255, 0.85);
}

.message-input {
  border-top: 1px solid var(--border-color);
  padding: 16px 20px 20px;
  background: #fff;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.tip {
  font-size: 12px;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .chat-page {
    padding: 16px;
  }

  .chat-header,
  .input-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .message-bubble {
    max-width: 82%;
  }
}
</style>
