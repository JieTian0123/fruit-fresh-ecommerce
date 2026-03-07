import request from '@/api/request'
import type { Conversation, SystemNotification, Message, ApiResponse, PageResult } from '@/types'

/**
 * 消息API模块
 * - 聊天功能使用Mock数据（WebSocket实时通信超出毕设范围）
 * - 系统通知使用真实后端API
 */

// ==================== 聊天相关 (Mock数据) ====================

const mockConversations: Conversation[] = [
  {
    id: 1,
    merchantId: 101,
    merchantName: '鲜果优选旗舰店',
    merchantAvatar: 'https://api.dicebear.com/7.x/initials/svg?seed=Fresh',
    shopName: '鲜果优选旗舰店',
    lastMessage: '您好，这批苹果什么时候能发货？',
    lastMessageTime: '2024-01-20 14:30',
    unreadCount: 0
  },
  {
    id: 2,
    merchantId: 102,
    merchantName: '热带水果专营店',
    merchantAvatar: 'https://api.dicebear.com/7.x/initials/svg?seed=Tropical',
    shopName: '热带水果专营店',
    lastMessage: '好的，我们会尽快为您处理',
    lastMessageTime: '2024-01-19 10:15',
    unreadCount: 0
  }
]

const mockMessages: Record<number, Message[]> = {
  101: [
    {
      id: 1,
      type: 'chat',
      senderId: 1,
      senderName: '我',
      content: '你好，我想咨询一下这批苹果的产地',
      isRead: true,
      createTime: '2024-01-20 14:20'
    },
    {
      id: 2,
      type: 'chat',
      senderId: 101,
      senderName: '鲜果优选旗舰店',
      senderAvatar: 'https://api.dicebear.com/7.x/initials/svg?seed=Fresh',
      content: '您好，我们的苹果来自新疆阿克苏，品质保证',
      isRead: true,
      createTime: '2024-01-20 14:22'
    },
    {
      id: 3,
      type: 'chat',
      senderId: 1,
      senderName: '我',
      content: '好的，那什么时候能发货？',
      isRead: true,
      createTime: '2024-01-20 14:25'
    },
    {
      id: 4,
      type: 'chat',
      senderId: 101,
      senderName: '鲜果优选旗舰店',
      senderAvatar: 'https://api.dicebear.com/7.x/initials/svg?seed=Fresh',
      content: '今天下单，明天就可以发货哦',
      isRead: true,
      createTime: '2024-01-20 14:30'
    }
  ],
  102: [
    {
      id: 5,
      type: 'chat',
      senderId: 1,
      senderName: '我',
      content: '芒果还有货吗？',
      isRead: true,
      createTime: '2024-01-19 10:10'
    },
    {
      id: 6,
      type: 'chat',
      senderId: 102,
      senderName: '热带水果专营店',
      senderAvatar: 'https://api.dicebear.com/7.x/initials/svg?seed=Tropical',
      content: '有的，现在库存充足',
      isRead: true,
      createTime: '2024-01-19 10:12'
    }
  ]
}

/**
 * 获取会话列表 (Mock)
 */
export function getConversations(): Promise<Conversation[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockConversations)
    }, 300)
  })
}

/**
 * 获取与商家的聊天记录 (Mock)
 */
export function getChatMessages(merchantId: number): Promise<Message[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([...(mockMessages[merchantId] || [])])
    }, 300)
  })
}

/**
 * 发送消息 (Mock)
 */
export function sendMessage(merchantId: number, content: string): Promise<Message> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const newMessage: Message = {
        id: Date.now(),
        type: 'chat',
        senderId: 1,
        senderName: '我',
        content,
        isRead: true,
        createTime: new Date().toISOString().replace('T', ' ').substring(0, 16)
      }
      
      if (!mockMessages[merchantId]) {
        mockMessages[merchantId] = []
      }
      mockMessages[merchantId].push(newMessage)
      
      resolve(newMessage)
    }, 300)
  })
}

/**
 * 标记聊天消息为已读 (Mock)
 */
export function markMessageAsRead(messageId: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => {
      Object.values(mockMessages).forEach(messages => {
        const msg = messages.find(m => m.id === messageId)
        if (msg) {
          msg.isRead = true
        }
      })
      resolve()
    }, 100)
  })
}

// ==================== 系统通知 (真实API) ====================

/**
 * 获取系统通知列表
 */
export async function getSystemNotifications(params?: {
  type?: string
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<SystemNotification>>> {
  const res = await request.get<ApiResponse<PageResult<SystemNotification>>>('/consumer/notification/list', { params })
  return res as unknown as ApiResponse<PageResult<SystemNotification>>
}

/**
 * 标记通知为已读
 */
export async function markNotificationAsRead(notificationId: number): Promise<ApiResponse> {
  const res = await request.put<ApiResponse>(`/consumer/notification/read/${notificationId}`)
  return res as unknown as ApiResponse
}

/**
 * 标记所有通知为已读
 */
export async function markAllNotificationsAsRead(): Promise<ApiResponse> {
  const res = await request.put<ApiResponse>('/consumer/notification/read-all')
  return res as unknown as ApiResponse
}

/**
 * 获取未读通知数量
 */
export async function getNotificationUnreadCount(): Promise<ApiResponse<number>> {
  const res = await request.get<ApiResponse<number>>('/consumer/notification/unread-count')
  return res as unknown as ApiResponse<number>
}

/**
 * 获取未读消息总数(聊天mock + 通知真实)
 */
export async function getUnreadCount(): Promise<number> {
  try {
    const res = await getNotificationUnreadCount()
    return (res.data as number) || 0
  } catch {
    return 0
  }
}
