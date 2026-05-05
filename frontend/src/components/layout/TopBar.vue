<template>
  <div class="top-bar">
    <div class="top-bar-content">
      <div class="left-links">
        <span v-if="!isLoggedIn" class="login-trigger" @click="router.push('/login')">亲，请登录</span>
        <span v-if="!isLoggedIn" class="register-trigger" @click="router.push('/register')">免费注册</span>
        
        <!-- 已登录：用户下拉菜单 -->
        <el-dropdown v-else trigger="click" @command="handleCommand" class="user-dropdown">
          <span class="username-trigger">
            hi，{{ username }} <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="orders">
                <el-icon><Document /></el-icon>我的订单
              </el-dropdown-item>
              <el-dropdown-item command="address">
                <el-icon><Location /></el-icon>收货地址
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      
      <div class="right-links">
        <div class="menu-item" @click="router.push('/')">
          <el-icon><House /></el-icon>
          首页
        </div>
        <div class="menu-item" @click="router.push('/messages')">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-icon><ChatDotRound /></el-icon>
          </el-badge>
          消息
        </div>
        <div class="menu-item" @click="router.push('/follow-shops')">
          <el-icon><Shop /></el-icon>
          关注店铺
        </div>
        <div class="menu-item" @click="router.push('/announcements')">
          <el-icon><Bell /></el-icon>
          平台公告
        </div>
        <div class="menu-item" @click="router.push('/orders')">我的订单</div>
        <div class="menu-item" @click="router.push('/cart')">
          <el-icon><ShoppingCart /></el-icon> 购物车 {{ cartCount }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { getUnreadCount } from '@/api/message'
import { House } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const username = computed(() => userStore.username)
const cartCount = computed(() => cartStore.cartCount)
const unreadCount = ref(0)
let pollingTimer: number | null = null

async function updateUnreadCount() {
  if (isLoggedIn.value) {
    try {
      unreadCount.value = await getUnreadCount()
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  } else {
    unreadCount.value = 0
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'address':
      router.push('/address')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

// 监听 MessagesView 中的已读事件，立即刷新未读计数
function onNotificationReadChanged() {
  updateUnreadCount()
}

onMounted(() => {
  updateUnreadCount()
  // Poll every 15 seconds
  pollingTimer = window.setInterval(updateUnreadCount, 15000)
  window.addEventListener('notification-read-changed', onNotificationReadChanged)
})

onUnmounted(() => {
  if (pollingTimer !== null) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  window.removeEventListener('notification-read-changed', onNotificationReadChanged)
})
</script>

<style scoped>
.top-bar {
  background-color: #f5f5f5;
  border-bottom: 1px solid #eee;
  font-size: 12px;
  color: #6C6C6C;
  height: 35px;
  line-height: 35px;
}

.top-bar-content {
  max-width: 1400px; /* Reduced from 1400 to match Taobao standard typically 1200, but keeping 1400 for modern wide screens */
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
}

.left-links, .right-links {
  display: flex;
  gap: 16px;
  align-items: center;
}

.location {
  margin-right: 8px;
}

.login-trigger {
  color: var(--color-primary);
  cursor: pointer;
}

.register-trigger {
  cursor: pointer;
}

.register-trigger:hover {
  color: var(--color-primary);
}

.username-trigger {
  color: var(--color-text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.username-trigger:hover {
  color: var(--color-primary);
}

.user-dropdown {
  margin-left: 8px;
}

.menu-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-item:hover {
  color: var(--color-primary);
}
</style>
