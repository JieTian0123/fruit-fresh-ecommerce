<template>
  <div class="user-dashboard-card">
    <!-- 用户信息区 -->
    <div class="user-profile">
      <div class="avatar-wrapper">
        <el-avatar :size="60" :src="getAvatarUrl(avatar)">
          <el-icon><User /></el-icon>
        </el-avatar>
      </div>
      <div class="welcome-text">
        <p class="greeting">Hi，{{ isLoggedIn ? (username || '会员') : '你好' }}</p>
        <p class="status-badge" v-if="isLoggedIn">
          <span class="badge">普通会员</span>
        </p>
      </div>
      
      <div class="action-buttons" v-if="!isLoggedIn">
        <el-button type="primary" round class="login-btn" @click="router.push('/login')">登录</el-button>
        <el-button round class="register-btn" @click="router.push('/register')">注册</el-button>
      </div>
    </div>
    
    <!-- 快捷功能区 -->
    <div class="quick-stats" v-if="isLoggedIn">
      <div class="stat-item" @click="router.push('/orders?status=0')">
        <div class="stat-num">0</div>
        <div class="stat-label">待付款</div>
      </div>
      <div class="stat-item" @click="router.push('/orders?status=2')">
        <div class="stat-num">0</div>
        <div class="stat-label">待收货</div>
      </div>
      <div class="stat-item" @click="router.push('/address')">
        <div class="stat-num"><el-icon><Location /></el-icon></div>
        <div class="stat-label">地址</div>
      </div>
      <div class="stat-item">
        <div class="stat-num">12</div>
        <div class="stat-label">关注店铺</div>
      </div>
    </div>
    
    <!-- 公告区 -->
    <div class="notice-section">
      <div class="notice-header">
        <span>平台公告</span>
        <span class="more">更多</span>
      </div>
      <ul class="notice-list">
        <li><span class="tag">热点</span> 新鲜车厘子到货通知</li>
        <li><span class="tag">优惠</span> 满199减30活动开启</li>
        <li><span class="tag">服务</span> 配送服务升级公告</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const username = computed(() => userStore.username)
const avatar = computed(() => userStore.avatar)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

function getAvatarUrl(avatar: string | undefined | null): string {
  if (!avatar) return defaultAvatar
  if (avatar.startsWith('/uploads/')) {
    return '/api' + avatar
  }
  return avatar
}
</script>

<style scoped>
.user-dashboard-card {
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  height: 100%;
  /* min-height removed */
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
}

.user-profile {
  padding: 30px 20px 20px;
  text-align: center;
  background-image: url('https://img.alicdn.com/imgextra/i2/O1CN01Z2kO6T1W5X6X5X6X5_!!6000000002738-2-tps-290-155.png'); /* Simulated bg */
  background-size: cover;
  background-position: center bottom;
  background-repeat: no-repeat;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.avatar-wrapper {
  margin-bottom: 12px;
  border: 3px solid white;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.greeting {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.badge {
  background: #FFF0E6;
  color: #FF5000;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.action-buttons {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.login-btn {
  width: 80px;
  font-weight: 600;
}

.register-btn {
  width: 80px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 20px 10px;
  border-bottom: 1px solid #f5f5f5;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  color: var(--color-text-secondary);
}

.stat-item:hover {
  color: var(--color-primary);
}

.stat-num {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
  color: var(--color-text-primary);
}

.stat-label {
  font-size: 12px;
}

.notice-section {
  padding: 16px;
  flex: 1;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

.notice-header .more {
  color: #999;
  font-weight: 400;
  font-size: 12px;
  cursor: pointer;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.notice-list li:hover {
  color: var(--color-primary);
}

.tag {
  color: #FF5000;
  background: #FFF0E6;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 12px;
  margin-right: 4px;
}
</style>
