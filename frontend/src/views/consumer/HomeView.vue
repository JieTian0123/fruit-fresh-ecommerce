<template>
  <div class="home-page">
    <!-- 顶部三栏布局 - 参考code.html -->
    <section class="top-grid-section">
      <!-- 左侧：商品分类 -->
      <div class="grid-left">
        <CategorySidebar />
      </div>

      <!-- 中间：领券入口 + 新鲜到店 -->
      <div class="grid-center">
        <div class="center-split">
          <!-- 领券中心固定入口 (2/3宽度) -->
          <div class="main-carousel-wrapper coupon-banner" @click="router.push('/coupons')">
            <div class="coupon-banner-content">
              <div class="coupon-banner-left">
                <div class="coupon-banner-tag">限时福利</div>
                <h2 class="coupon-banner-title">领券中心</h2>
                <p class="coupon-banner-sub">新人专享 · 每日限量 · 满减优惠</p>
                <el-button type="warning" size="default" round class="coupon-banner-btn">
                  立即领券
                </el-button>
              </div>
              <div class="coupon-banner-right">
                <div class="coupon-card-preview">
                  <div class="preview-card preview-card-1">
                    <span class="preview-symbol">¥</span>
                    <span class="preview-value">5</span>
                  </div>
                  <div class="preview-card preview-card-2">
                    <span class="preview-symbol">¥</span>
                    <span class="preview-value">10</span>
                  </div>
                  <div class="preview-card preview-card-3">
                    <span class="preview-symbol">¥</span>
                    <span class="preview-value">20</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 新鲜到店 (1/3宽度) -->
          <div class="daily-fresh activity-card" @click="goActivity(dailyActivity)">
            <div class="daily-header">
              <span class="daily-title">{{ dailyActivity.title }}</span>
              <span class="daily-badge">{{ dailyActivity.badge }}</span>
            </div>
            <div class="daily-activity-body">
              <img
                :src="resolveActivityImage(dailyActivity)"
                :alt="dailyActivity.title"
                class="daily-activity-img"
                @error="handleActivityImageError"
              />
              <div class="daily-activity-info">
                <span class="daily-name">{{ dailyActivity.subtitle }}</span>
                <el-button type="success" size="small" class="daily-action-btn" round>
                  {{ dailyActivity.actionText }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 下方四个功能块：2x2网格 -->
        <div class="feature-blocks">
          <!-- 1. 临期特惠 -->
          <div class="feature-block block-new" @click="goActivity(newTasteActivity)">
            <div class="feature-header">
              <span class="feature-title">{{ newTasteActivity.title }}</span>
              <span class="new-badge">{{ newTasteActivity.badge }}</span>
            </div>
            <div class="feature-content">
              <img
                :src="resolveActivityImage(newTasteActivity)"
                :alt="newTasteActivity.title"
                class="new-product-img"
                @error="handleActivityImageError"
              />
              <span class="block-value new-text">{{ newTasteActivity.subtitle }}</span>
            </div>
            <el-button type="success" size="small" class="block-action-btn" round>{{ newTasteActivity.actionText }}</el-button>
          </div>

          <!-- 2. 热销榜单 -->
          <div class="feature-block block-discount" @click="goActivity(fullReductionActivity)">
            <div class="feature-header">
              <span class="feature-title">{{ fullReductionActivity.title }}</span>
              <el-icon class="feature-icon discount-icon"><Present /></el-icon>
            </div>
            <div class="feature-content">
              <img
                :src="resolveActivityImage(fullReductionActivity)"
                :alt="fullReductionActivity.title"
                class="new-product-img"
                @error="handleActivityImageError"
              />
              <span class="block-value discount-text">{{ fullReductionActivity.badge || fullReductionActivity.subtitle }}</span>
            </div>
            <el-button type="warning" size="small" class="block-action-btn" round>{{ fullReductionActivity.actionText }}</el-button>
          </div>
        </div>
      </div>

      <!-- 右侧：用户卡片 + 快捷入口 + 积分 + 快报 -->
      <div class="grid-right">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <div class="user-avatar-wrapper">
            <el-avatar :size="40" :src="getAvatarUrl(userStore.avatar)">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <h3 class="user-greeting">{{ isLoggedIn ? `Hi，${userStore.username || '会员'}` : '下午好！' }}</h3>
          <div class="user-actions" v-if="!isLoggedIn">
            <el-button type="primary" size="small" round @click="router.push('/login')">登录</el-button>
            <el-button size="small" round @click="router.push('/register')">注册</el-button>
          </div>
        </div>

        <!-- 快捷功能入口 -->
      <div class="quick-entry">
        <div class="entry-item" @click="router.push('/orders?status=0')">
          <el-icon><Wallet /></el-icon>
          <span>待付款</span>
        </div>
        <div class="entry-item" @click="router.push('/orders?status=1')">
          <el-icon><Van /></el-icon>
          <span>待发货</span>
        </div>
        <div class="entry-item" @click="router.push('/orders?status=2')">
          <el-icon><Box /></el-icon>
          <span>待收货</span>
        </div>
        <div class="entry-item" @click="router.push('/orders?status=review')">
          <el-icon><ChatDotRound /></el-icon>
          <span>待评价</span>
        </div>
      </div>

      <!-- 积分签到 -->
      <div class="points-card" @click="handleSignIn">
        <div class="points-info">
          <span class="points-title">鲜果积分</span>
          <span class="points-desc">每日签到领积分</span>
        </div>
        <el-button type="warning" size="small" round class="sign-btn" :disabled="signedToday">
          {{ signedToday ? '已签到' : '立即签到' }}
        </el-button>
      </div>

      <!-- 会员中心入口 -->
      <div class="vip-entry-card" @click="router.push('/member')">
        <div class="vip-entry-icon">👑</div>
        <div class="vip-entry-info">
          <span class="vip-entry-title">会员中心</span>
          <span class="vip-entry-desc">专属权益等你来</span>
        </div>
        <el-icon class="vip-entry-arrow"><ArrowRight /></el-icon>
      </div>
      </div>
    </section>

    <!-- 商品推荐区域 -->
    <section class="product-section">
      <div class="section-header">
        <h2 class="section-title">为你推荐</h2>
      </div>

      <!-- 商品网格 -->
      <div class="product-grid" v-loading="loading">
        <ProductCard
          v-for="product in displayProducts"
          :key="product.id"
          :product="product"
          @add-cart="handleAddCart"
        />
      </div>

      <!-- 加载更多 -->
      <div class="load-more">
        <el-button v-if="hasMore" :loading="loadingMore" @click="loadMore" round>
          加载更多
        </el-button>
        <div v-else class="no-more">—— 到底了，再去逛逛吧 ——</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductList } from '@/api/product'
import { getHomeActivities } from '@/api/home'
import { signIn as signInApi, getSignInStatus } from '@/api/points'
import ProductCard from '@/components/ProductCard.vue'
import CategorySidebar from '@/components/home/CategorySidebar.vue'
import type { Product, ProductQuery, HomeActivity } from '@/types'
import { ElMessage } from 'element-plus'
import {
  DEFAULT_HOME_ACTIVITIES,
  loadStoredHomeActivities,
  mergeHomeActivities,
  saveHomeActivities
} from '@/utils/homeActivities'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取完整的头像URL
function getAvatarUrl(avatar: string | undefined | null): string {
  if (!avatar) return defaultAvatar
  if (avatar.startsWith('/uploads/')) return '/api' + avatar
  return avatar
}

const isLoggedIn = computed(() => userStore.isLoggedIn)
const signedToday = ref(false)

// 退出登录后重置签到状态
watch(isLoggedIn, (val) => {
  if (!val) {
    signedToday.value = false
  }
})

// 数据状态
const loading = ref(false)
const loadingMore = ref(false)
const displayProducts = ref<Product[]>([])
const pageNumber = ref(1)
const hasMore = ref(true)
const pageSize = 12

const defaultActivityImage = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="360" height="220" viewBox="0 0 360 220"><rect width="360" height="220" fill="#f0fdf4"/><circle cx="250" cy="70" r="52" fill="#bbf7d0"/><circle cx="130" cy="128" r="58" fill="#fed7aa"/><text x="180" y="122" text-anchor="middle" font-size="58">🍊</text></svg>')

const homeActivities = ref<HomeActivity[]>(loadStoredHomeActivities())

const activityMap = computed(() => {
  const map = new Map<string, HomeActivity>()
  DEFAULT_HOME_ACTIVITIES.forEach(activity => map.set(activity.code, activity))
  homeActivities.value
    .filter(activity => activity.status === 1)
    .forEach(activity => {
      const fallback = map.get(activity.code)
      map.set(activity.code, { ...fallback, ...activity } as HomeActivity)
    })
  return map
})

const dailyActivity = computed<HomeActivity>(() => activityMap.value.get('DAILY_NEW') || (DEFAULT_HOME_ACTIVITIES[0] as HomeActivity))
const newTasteActivity = computed<HomeActivity>(() => activityMap.value.get('NEW_TASTE') || (DEFAULT_HOME_ACTIVITIES[1] as HomeActivity))
const fullReductionActivity = computed<HomeActivity>(() => activityMap.value.get('FULL_REDUCTION') || (DEFAULT_HOME_ACTIVITIES[2] as HomeActivity))

function resolveActivityImage(activity: HomeActivity) {
  if (!activity.imageUrl) return defaultActivityImage
  return activity.imageUrl
}

function handleActivityImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = defaultActivityImage
}

function goActivity(activity: HomeActivity) {
  if (!activity.linkUrl) return
  if (/^https?:\/\//.test(activity.linkUrl)) {
    window.open(activity.linkUrl, '_blank')
    return
  }
  router.push(activity.linkUrl)
}

async function loadHomeActivities() {
  homeActivities.value = loadStoredHomeActivities()
  try {
    const res = await getHomeActivities()
    if (res.data?.length) {
      homeActivities.value = mergeHomeActivities(res.data)
      saveHomeActivities(homeActivities.value)
    }
  } catch {
    homeActivities.value = loadStoredHomeActivities()
  }
}

// 加载商品
async function loadProducts(isInit = false) {
  if (isInit) loading.value = true
  else loadingMore.value = true

  try {
    const params: ProductQuery = {
      pageNum: pageNumber.value,
      pageSize: pageSize,
      sortBy: 'random',
      sortOrder: 'desc'
    }

    const res = await getProductList(params)
    const list = res.data.list || res.data.records || []

    if (isInit) {
      displayProducts.value = list
    } else {
      displayProducts.value.push(...list)
    }

    if (list.length < pageSize) {
      hasMore.value = false
    } else {
      pageNumber.value++
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  await loadProducts(false)
}

// 加入购物车
async function handleAddCart(product: Product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await cartStore.addToCart(product.id, 1)
}

// 签到
async function handleSignIn() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (signedToday.value) {
    ElMessage.info('今日已签到')
    return
  }
  try {
    const res = await signInApi()
    signedToday.value = true
    ElMessage.success('签到成功！')
  } catch {
    // error already handled by interceptor
  }
}

onMounted(async () => {
  loadHomeActivities()
  loadProducts(true)
  if (userStore.isLoggedIn) {
    try {
      const res = await getSignInStatus()
      signedToday.value = (res.data as any)?.signedToday ?? false
    } catch {
      // ignore
    }
  }
})
</script>

<style scoped>
.home-page {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 10px;
}

/* 顶部三栏布局 */
.top-grid-section {
  display: grid;
  grid-template-columns: 200px 1fr 280px;
  gap: 12px;
  margin-bottom: 24px;
  align-items: stretch;
  /* 去掉 min-height，或者设置固定的 height: 360px 以保证三栏强制同高 */
  height: 340px;
}

.grid-left {
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
  max-height: none; /* 去掉之前的 max-height 限制，让它跟随 stretch */
  display: flex;
  flex-direction: column;
}
/* 如果需要在 HomeView 中强制其内部撑满，可以利用 :deep(.category-sidebar) */
.grid-left :deep(.category-sidebar) {
  height: 100%;
  flex: 1;
  overflow-y: auto;
}

.grid-center {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  height: 100%;
  overflow: visible;
}

/* 中间区域拆分：领券入口 + 新鲜到店 */
.center-split {
  display: flex;
  gap: 12px;
  height: 200px;
  flex-shrink: 0;
}

.main-carousel-wrapper {
  flex: 2;
  border-radius: 12px;
  overflow: hidden;
}

.daily-fresh {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.activity-card {
  cursor: pointer;
  transition: all 0.2s;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(76, 175, 80, 0.16);
}

.daily-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.daily-title {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.daily-badge {
  font-size: 10px;
  background: #fff0f0;
  color: #ff4d4f;
  padding: 2px 6px;
  border-radius: 4px;
}

.daily-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.daily-activity-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.daily-activity-img {
  width: 100%;
  height: 104px;
  border-radius: 8px;
  object-fit: cover;
  background: #f8f9fa;
}

.daily-activity-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.daily-action-btn {
  flex-shrink: 0;
}

.daily-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.daily-item:hover {
  background: #f0f0f0;
}

.daily-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
}

.daily-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.daily-name {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.daily-price {
  font-size: 14px;
  font-weight: 600;
  color: #ff9800;
}

.daily-discount {
  position: absolute;
  top: 0;
  right: 0;
  background: #ff9800;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 0 8px 0 8px;
}

/* 领券入口卡片样式 */
.coupon-banner {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 50%, #ffcd3c 100%);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: stretch;
}

.coupon-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.35);
}

.coupon-banner-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
}

.coupon-banner-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coupon-banner-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
  width: fit-content;
}

.coupon-banner-title {
  font-size: 26px;
  font-weight: 800;
  color: white;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.coupon-banner-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.coupon-banner-btn {
  margin-top: 4px;
  font-weight: 600;
  width: fit-content;
}

.coupon-banner-right {
  display: flex;
  align-items: center;
}

.coupon-card-preview {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.preview-card {
  display: flex;
  align-items: baseline;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  padding: 8px 10px;
  color: white;
  font-weight: 800;
  gap: 1px;
}

.preview-card-1 { transform: rotate(-5deg) translateY(4px); }
.preview-card-2 { transform: translateY(-2px); }
.preview-card-3 { transform: rotate(5deg) translateY(4px); }

.preview-symbol { font-size: 14px; }
.preview-card-1 .preview-value { font-size: 22px; }
.preview-card-2 .preview-value { font-size: 28px; }
.preview-card-3 .preview-value { font-size: 22px; }

/* 轮播图样式（已废弃，保留空注释） */

/* 功能块区域 */
.feature-blocks {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  flex: 1; /* 让其在 center 中自动撑满剩余高度 */
}

.feature-block {
  background: white;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  min-height: 96px;
  overflow: hidden;
  position: relative;
}

.feature-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.feature-title {
  font-weight: 700;
  font-size: 14px;
  color: #333;
}

.feature-icon {
  font-size: 18px;
}

.flash-badge {
  font-size: 10px;
  background: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}

.new-badge {
  font-size: 10px;
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}

.feature-content {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  flex: 1;
}

.block-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.flash-icon { background: #fff0f0; color: #ff4d4f; }
.group-bg { background: #fff0f6; color: #ff69b4; }
.discount-bg { background: #fff7e6; color: #ff9800; }

.block-value {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flash-text { color: #ff4d4f; }
.group-text { color: #ff69b4; }
.new-text { color: #52c41a; }
.discount-text { color: #ff9800; }

.new-product-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  background: #f8f9fa;
  flex-shrink: 0;
}

.block-action-btn {
  align-self: flex-end;
  margin-top: auto;
  opacity: 0.9;
  transition: opacity 0.2s;
}

.feature-block:hover .block-action-btn {
  opacity: 1;
}

.block-flash:hover { border-color: #ffccc7; }
.block-group:hover { border-color: #ffd6e7; }
.block-new:hover { border-color: #b7eb8f; }
.block-discount:hover { border-color: #ffe58f; }

/* 右侧用户卡片 */
.grid-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 10px 14px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 36px;
  background: linear-gradient(135deg, #f0f9eb 0%, #e8f5e9 100%);
}

.user-avatar-wrapper {
  position: relative;
  z-index: 1;
  margin-bottom: 4px;
}

.user-greeting {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.user-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 快捷入口 */
.quick-entry {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  padding: 10px 10px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 6px 4px;
  border-radius: 8px;
  transition: all 0.2s;
}

.entry-item:hover {
  background: #f5f5f5;
}

.entry-item .el-icon {
  font-size: 20px;
  color: #999;
}

.entry-item:hover .el-icon {
  color: #4caf50;
}

.entry-item span {
  font-size: 11px;
  color: #666;
}

/* 积分卡片 */
.points-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  border-radius: 12px;
  cursor: pointer;
  border: 1px solid #ffe0b2;
}

.points-title {
  font-weight: 600;
  font-size: 13px;
  color: #333;
  display: block;
}

.points-desc {
  font-size: 11px;
  color: #999;
}

.sign-btn {
  font-size: 11px;
  padding: 6px 12px;
}

/* VIP会员中心入口卡片 */
.vip-entry-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #fdf2e9 0%, #fce8d5 100%);
  border-radius: 12px;
  cursor: pointer;
  border: 1px solid #f5d0a9;
  transition: all 0.2s;
}

.vip-entry-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.15);
}

.vip-entry-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.vip-entry-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.vip-entry-title {
  font-size: 14px;
  font-weight: 600;
  color: #b8741a;
}

.vip-entry-desc {
  font-size: 11px;
  color: #c49048;
}

.vip-entry-arrow {
  color: #c49048;
  font-size: 14px;
}

.shop-entry-card {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-color: #a5d6a7;
}

.shop-entry-card:hover {
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.15);
}

.shop-entry-card .vip-entry-title {
  color: #2e7d32;
}

.shop-entry-card .vip-entry-desc {
  color: #4caf50;
}

.shop-entry-card .vip-entry-arrow {
  color: #4caf50;
}

/* 商品推荐区域 */
.product-section {
  margin-top: 24px;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #4caf50;
  border-radius: 2px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.load-more {
  margin-top: 40px;
  text-align: center;
  padding-bottom: 40px;
}

.no-more {
  color: #999;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .top-grid-section {
    grid-template-columns: 180px 1fr 260px;
  }
}

@media (max-width: 992px) {
  .top-grid-section {
    grid-template-columns: 180px 1fr;
  }
  .grid-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .top-grid-section {
    grid-template-columns: 1fr;
    height: auto;
  }
  .grid-left {
    display: none;
  }
  .center-split {
    flex-direction: column;
    height: auto;
  }
  .main-carousel-wrapper {
    height: 180px;
  }
  .daily-fresh {
    height: 180px;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
