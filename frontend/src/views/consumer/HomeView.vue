<template>
  <div class="home-page">
    <!-- 顶部三栏布局 - 参考code.html -->
    <section class="top-grid-section">
      <!-- 左侧：商品分类 -->
      <div class="grid-left">
        <CategorySidebar />
      </div>

      <!-- 中间：领券入口 + 每日上新 -->
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

          <!-- 每日上新 (1/3宽度) -->
          <div class="daily-fresh">
            <div class="daily-header">
              <span class="daily-title">每日上新</span>
              <span class="daily-badge">热卖</span>
            </div>
            <div class="daily-items">
              <div 
                v-for="item in dailyFresh" 
                :key="item.id" 
                class="daily-item"
                @click="router.push(`/product/${item.productId}`)"
              >
                <img :src="item.image" :alt="item.name" class="daily-img" />
                <div class="daily-info">
                  <span class="daily-name">{{ item.name }}</span>
                  <span class="daily-price">¥{{ item.price.toFixed(2) }}</span>
                </div>
                <span v-if="item.discount" class="daily-discount">-{{ item.discount }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 下方三个功能块：领券中心 + 新品尝鲜 + 全球进口 -->
        <div class="feature-blocks">
          <div class="feature-block" @click="router.push('/coupons')">
            <div class="feature-header">
              <span class="feature-title">领券中心</span>
              <el-icon class="feature-icon coupon"><Ticket /></el-icon>
            </div>
            <div class="feature-content">
              <div class="coupon-icon-wrapper">
                <el-icon :size="28"><Coupon /></el-icon>
              </div>
              <span class="coupon-value">¥20 优惠券</span>
            </div>
          </div>

          <div class="feature-block" @click="router.push('/category?tag=new')">
            <div class="feature-header">
              <span class="feature-title">新品尝鲜</span>
              <span class="new-badge">NEW</span>
            </div>
            <div class="feature-content">
              <img 
                src="https://images.unsplash.com/photo-1568702846914-96b305d2uj67?w=80&h=80&fit=crop" 
                alt="新品" 
                class="new-product-img"
              />
              <span class="new-label">环球甄选</span>
            </div>
          </div>

          <div class="feature-block" @click="router.push('/category/3')">
            <div class="feature-header">
              <span class="feature-title">全球进口</span>
              <el-icon class="feature-icon global"><Ship /></el-icon>
            </div>
            <div class="feature-content">
              <el-icon :size="32" class="global-icon"><LocationOn /></el-icon>
              <span class="global-label">品质保证</span>
            </div>
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
          <span class="points-desc">100积分</span>
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
import { signIn as signInApi, getSignInStatus } from '@/api/points'
import ProductCard from '@/components/ProductCard.vue'
import CategorySidebar from '@/components/home/CategorySidebar.vue'
import type { Product, ProductQuery } from '@/types'
import { ElMessage } from 'element-plus'

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

// 每日上新数据
const dailyFresh = [
  {
    id: 1,
    productId: 101,
    name: '丹东草莓',
    price: 29.90,
    image: 'https://images.unsplash.com/photo-1568702846914-96b305d2uj67?w=100&h=100&fit=crop',
    discount: 20
  },
  {
    id: 2,
    productId: 102,
    name: '有机香蕉',
    price: 9.90,
    image: 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=100&h=100&fit=crop',
    discount: null
  }
]

// 加载商品
async function loadProducts(isInit = false) {
  if (isInit) loading.value = true
  else loadingMore.value = true

  try {
    const params: ProductQuery = {
      pageNum: pageNumber.value,
      pageSize: pageSize,
      sortBy: 'sales',
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
    const points = (res.data as any)?.pointsEarned
    ElMessage.success(points ? `签到成功，获得${points}积分！` : '签到成功！')
  } catch {
    // error already handled by interceptor
  }
}

onMounted(async () => {
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
}

.grid-left {
  border-radius: 12px;
  overflow: hidden;
}

.grid-center {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
}

/* 中间区域拆分：轮播图 + 每日上新 */
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
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
  align-items: stretch;
}

.feature-block {
  background: white;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  min-height: 96px;
}

.feature-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.feature-title {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.feature-icon {
  font-size: 18px;
}

.feature-icon.coupon {
  color: #ff4d4f;
}

.feature-icon.global {
  color: #1890ff;
}

.new-badge {
  font-size: 10px;
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 0;
  flex: 1;
}

.coupon-icon-wrapper {
  width: 40px;
  height: 40px;
  background: #fff0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff4d4f;
}

.coupon-value {
  font-size: 12px;
  font-weight: 600;
  color: #ff4d4f;
}

.new-product-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
}

.new-label {
  font-size: 12px;
  color: #1890ff;
}

.global-icon {
  color: #1890ff;
}

.global-label {
  font-size: 12px;
  color: #1890ff;
}

/* 右侧用户卡片 */
.grid-right {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
