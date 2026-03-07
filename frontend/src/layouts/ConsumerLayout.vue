<template>
  <div class="consumer-layout">
    <!-- 顶部工具栏 -->
    <TopBar />

    <!-- 顶部导航/搜索区 -->
    <header class="main-header" v-if="route.path === '/'">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <el-icon class="logo-icon"><Apple /></el-icon>
          <span class="logo-text">水果生鲜</span>
        </div>
        
        <!-- 居中搜索框 -->
        <div class="search-section">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              placeholder="搜索新鲜水果、蔬菜..."
              @keyup.enter="handleSearch"
            />
            <span v-if="searchKeyword" class="search-clear" @click="searchKeyword = ''">✕</span>
            <button @click="handleSearch">搜索</button>
          </div>
        </div>
        
        <!-- 右侧二维码/购物车 -->
        <div class="header-right">
          <div class="cart-btn" @click="router.push('/cart')">
            <el-icon size="24"><ShoppingCart /></el-icon>
            <span>我的购物车</span>
            <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99" class="cart-badge" />
          </div>
        </div>
      </div>
    </header>

    <!-- 子页面导航栏（非首页显示） -->
    <header class="sub-header" v-if="route.path !== '/'">
      <div class="sub-header-content">
        <div class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <h3 class="page-title">{{ route.meta.title || '水果生鲜' }}</h3>
        <div class="sub-header-right"></div>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <main class="main-content">
      <router-view />
    </main>
    
    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-bottom">
          <p>© 2026 水果生鲜电商平台 | 消费者维权热线：12315</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import TopBar from '@/components/layout/TopBar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

const isLoggedIn = computed(() => userStore.isLoggedIn)
const cartCount = computed(() => cartStore.cartCount)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { keyword: searchKeyword.value.trim() } })
  }
}

function loadCart() {
  if (userStore.isLoggedIn) {
    cartStore.fetchCartList()
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.consumer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-background);
}

/* Header */
.main-header {
  background: white;
  padding: 16px 0;
}

/* Sub-page Header */
.sub-header {
  background: white;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.sub-header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.sub-header-right {
  width: 80px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 14px;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  transition: all 0.2s;
  flex-shrink: 0;
}

.back-btn:hover {
  color: var(--color-primary);
  background: #f0f9f0;
}

.back-btn .el-icon {
  font-size: 18px;
}

.header-content {
  max-width: 1400px; /* Reduced to align with Taobao standard width if needed, but 1400 is fine */
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  width: 200px;
}

.logo-icon {
  font-size: 48px;
  color: var(--color-primary);
}

.logo-text {
  font-family: var(--font-heading);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-primary);
}

/* Search Section - Taobao Style */
.search-section {
  flex: 1;
  max-width: 700px;
}

.search-box {
  display: flex;
  height: 42px;
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-full);
  overflow: hidden;
  transition: all 0.3s;
}

.search-box:focus-within {
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 20px;
  font-size: 14px;
}

.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  flex-shrink: 0;
  transition: color 0.2s;
}

.search-clear:hover {
  color: #333;
}

.search-box button {
  width: 100px;
  background: var(--color-primary); /* Solid Primary */
  color: white;
  border: none;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.search-box button:hover {
  background: var(--color-primary-dark);
}

/* Header Right */
.header-right {
  width: 200px;
  display: flex;
  justify-content: flex-end;
}

.cart-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #eee;
  border-radius: var(--radius-full);
  background: #fdfdfd;
  color: var(--color-primary);
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.cart-btn:hover {
  background: white;
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

/* Main Content */
.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  width: 100%;
}

/* Footer Simplification */
.footer {
  background: white; /* Clean white footer */
  border-top: 1px solid #eee;
  padding: 24px 0;
  text-align: center;
  color: #999;
}
</style>
