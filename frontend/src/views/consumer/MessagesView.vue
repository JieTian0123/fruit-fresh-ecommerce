<template>
  <div class="messages-page">
    <h2>我的消息</h2>
    
    <el-tabs v-model="activeTab" class="message-tabs">
      <!-- 聊天会话 -->
      <el-tab-pane label="聊天" name="chat">
        <el-badge :value="totalChatUnread" :hidden="totalChatUnread === 0" :max="99" />
        
        <div v-if="conversations.length === 0" class="empty-state">
          <el-empty description="暂无聊天记录" />
        </div>
        
        <div v-else class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            @click="openChat(conv.merchantId)"
          >
            <el-badge :value="conv.unreadCount" :hidden="conv.unreadCount === 0" class="badge">
              <el-avatar :size="50" :src="conv.merchantAvatar">
                {{ conv.merchantName[0] }}
              </el-avatar>
            </el-badge>
            
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="merchant-name">{{ conv.shopName || conv.merchantName }}</span>
                <span class="time">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="last-message">{{ conv.lastMessage }}</div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 系统通知 -->
      <el-tab-pane label="通知" name="notification">
        <div class="notification-toolbar">
          <el-badge :value="totalNotificationUnread" :hidden="totalNotificationUnread === 0" :max="99" />
          <el-button v-if="totalNotificationUnread > 0" type="primary" link size="small" @click="handleMarkAllRead">全部标为已读</el-button>
        </div>
        
        <div v-if="notifications.length === 0" class="empty-state">
          <el-empty description="暂无系统通知" />
        </div>
        
        <div v-else class="notification-list">
          <div
            v-for="notif in notifications"
            :key="notif.id"
            class="notification-item"
            :class="{ unread: !notif.isRead }"
            @click="handleNotificationClick(notif)"
          >
            <div class="notification-icon" :class="`type-${notif.type}`">
              <el-icon v-if="notif.type === 'order'"><ShoppingCart /></el-icon>
              <el-icon v-else-if="notif.type === 'promotion'"><Present /></el-icon>
              <el-icon v-else><Bell /></el-icon>
            </div>
            
            <div class="notification-content">
              <div class="notification-header">
                <span class="title">{{ notif.title }}</span>
                <span class="time">{{ formatTime(notif.createTime) }}</span>
              </div>
              <div class="content">{{ notif.content }}</div>
            </div>
            
            <div v-if="!notif.isRead" class="unread-dot"></div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 聊天对话框 (简化版，不单独创建ChatView页面) -->
    <el-drawer
      v-model="chatDrawerVisible"
      :title="currentMerchantName"
      size="40%"
      direction="rtl"
    >
      <div class="chat-container">
        <div ref="messageListRef" class="message-list">
          <div v-for="msg in chatMessages" :key="msg.id" class="message-item" :class="{ self: msg.senderId === currentUserId }">
            <el-avatar v-if="msg.senderId !== currentUserId" :size="36" :src="msg.senderAvatar">
              {{ msg.senderName?.[0] }}
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
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="handleSendMessage"
          />
          <el-button type="primary" :loading="sending" @click="handleSendMessage">
            发送
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { Conversation, SystemNotification, Message } from '@/types'
import {
  getConversations,
  getSystemNotifications,
  getChatMessages,
  sendMessage,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from '@/api/message'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const activeTab = ref('chat')

const conversations = ref<Conversation[]>([])
const notifications = ref<SystemNotification[]>([])
const chatMessages = ref<Message[]>([])

const chatDrawerVisible = ref(false)
const currentMerchantId = ref<number | null>(null)
const currentMerchantName = ref('')
const messageInput = ref('')
const sending = ref(false)
const messageListRef = ref<HTMLElement>()

const currentUserId = ref(1) // 当前用户ID，应从store获取

const totalChatUnread = computed(() => {
  return conversations.value.reduce((sum, conv) => sum + conv.unreadCount, 0)
})

const totalNotificationUnread = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

function formatTime(time: string) {
  const now = new Date()
  const msgTime = new Date(time)
  const diff = now.getTime() - msgTime.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return formatDateTime(time).substring(0, 16)
}

function getNotificationOrderNo(notif: SystemNotification) {
  const text = `${notif.title || ''} ${notif.content || ''}`
  const match = text.match(/#\s*(\d{10,})/) || text.match(/订单[^\d]*(\d{10,})/)
  return match?.[1]
}

async function loadConversations() {
  conversations.value = await getConversations()
}

async function loadNotifications() {
  try {
    const res = await getSystemNotifications({ pageNum: 1, pageSize: 50 })
    const records = (res.data as any)?.records || (res.data as any)?.list || []
    // 后端 isRead 是 Integer(0/1)，前端需要 boolean
    notifications.value = records.map((n: any) => ({
      ...n,
      isRead: n.isRead === true || n.isRead === 1
    }))
  } catch {
    notifications.value = []
  }
}

async function openChat(merchantId: number) {
  router.push(`/chat/${merchantId}`)
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSendMessage(e?: Event) {
  if (e) e.preventDefault()
  if (!messageInput.value.trim() || !currentMerchantId.value || sending.value) return
  
  sending.value = true
  try {
    const newMessage = await sendMessage(currentMerchantId.value, messageInput.value.trim())
    chatMessages.value.push(newMessage)
    messageInput.value = ''
    
    // 更新会话列表
    const conv = conversations.value.find(c => c.merchantId === currentMerchantId.value)
    if (conv) {
      conv.lastMessage = newMessage.content
      conv.lastMessageTime = newMessage.createTime
    }
    
    await nextTick()
    scrollToBottom()
  } finally {
    sending.value = false
  }
}

async function handleNotificationClick(notif: SystemNotification) {
  if (!notif.isRead) {
    try {
      await markNotificationAsRead(notif.id)
      notif.isRead = true
      // 同步更新TopBar的未读计数
      window.dispatchEvent(new CustomEvent('notification-read-changed'))
    } catch (err) {
      console.error('Failed to mark notification as read:', err)
    }
  }
  
  if (notif.type === 'order') {
    const orderNo = getNotificationOrderNo(notif)
    router.push(orderNo ? `/order/${orderNo}` : '/orders')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllNotificationsAsRead()
    notifications.value.forEach(n => { n.isRead = true })
    // 同步更新TopBar的未读计数
    window.dispatchEvent(new CustomEvent('notification-read-changed'))
  } catch (err) {
    console.error('Failed to mark all notifications as read:', err)
  }
}

onMounted(async () => {
  await loadConversations()
  loadNotifications()

  // 如果携带 merchantId 参数，自动打开对应聊天
  const merchantIdParam = route.query.merchantId
  if (merchantIdParam) {
    const merchantId = Number(merchantIdParam)
    router.replace(`/chat/${merchantId}`)
  }
})
</script>

<style scoped>
.messages-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.messages-page h2 {
  margin-bottom: 24px;
}

.message-tabs {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.empty-state {
  padding: 60px 0;
}

.notification-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 会话列表 */
.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.conversation-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.merchant-name {
  font-weight: 600;
  font-size: 15px;
}

.time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.last-message {
  font-size: 14px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 通知列表 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.notification-item.unread {
  background: #e8f4fd;
}

.notification-item:hover {
  background: #f0f0f0;
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.notification-icon.type-order {
  background: #e8f5e9;
  color: #228B22;
}

.notification-icon.type-promotion {
  background: #fff3e0;
  color: #FF8C00;
}

.notification-icon.type-system {
  background: #e3f2fd;
  color: #1976D2;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notification-header .title {
  font-weight: 600;
  font-size: 15px;
}

.notification-content .content {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.unread-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
}

/* 聊天界面 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  max-width: 60%;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: #f5f5f5;
}

.message-item.self .message-bubble {
  background: var(--color-primary);
  color: white;
}

.sender-name {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  text-align: right;
}

.message-item.self .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message-input {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
