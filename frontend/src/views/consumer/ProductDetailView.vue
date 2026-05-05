<template>
  <div class="product-detail-page" v-loading="loading">
    <template v-if="product">
      <!-- 商品信息区 -->
      <div class="product-info-section">
        <!-- 商品图片 -->
        <div class="product-gallery">
          <div class="main-image">
            <img :src="displayImage" :alt="product.name" @error="handleImageError" />
          </div>
          <div class="thumbnail-list" v-if="imageList.length > 1">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="thumbnail"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" :alt="`${product.name}-${index}`" @error="handleImageError" />
            </div>
          </div>
        </div>
        
        <!-- 商品详情 -->
        <div class="product-details">
          <h1 class="product-name">{{ product.name }}</h1>
          
          <p class="product-desc">{{ product.description || '新鲜直达，品质保证' }}</p>
          
          <!-- 店铺信息 -->
          <div class="shop-info-card" v-if="product.merchantName">
            <div class="shop-header">
              <div class="shop-left">
                <el-avatar :size="50" :src="normalizeImageUrl(product.shopLogo)">
                  <el-icon><Shop /></el-icon>
                </el-avatar>
                <div class="shop-text">
                  <h4>{{ product.merchantName }}</h4>
                  <p>店铺编号: {{ product.merchantId }}</p>
                </div>
              </div>
              <div class="shop-actions">
                <el-button 
                  :type="isFollowed ? 'default' : 'primary'" 
                  :icon="isFollowed ? Star : StarFilled"
                  @click="handleToggleFollow"
                  :loading="followLoading"
                >
                  {{ isFollowed ? '取消关注' : '关注店铺' }}
                </el-button>
                <el-button @click="visitShop">进入店铺</el-button>
                <el-button type="info" plain @click="contactService">
                  <el-icon><ChatDotRound /></el-icon>
                  联系客服
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="price-section">
            <div class="price-wrapper">
              <span class="label">价格</span>
              <span class="currency">¥</span>
              <span class="price">{{ (product.currentPrice ?? product.price)?.toFixed(2) }}</span>
              <span class="unit">/ {{ product.unit || '500g' }}</span>
              <template v-if="product.currentPrice && product.currentPrice < product.price">
                <span class="original-price">¥{{ product.price?.toFixed(2) }}</span>
                <el-tag type="danger" size="small" class="discount-tag">{{ product.discountLabel }}</el-tag>
              </template>
            </div>
          </div>
          
          <div class="info-row">
            <span class="label">销量</span>
            <span class="value">{{ product.sales || 0 }}件</span>
          </div>
          
          <div class="info-row">
            <span class="label">库存</span>
            <span class="value" :class="{ 'low-stock': product.stock <= 10 }">
              {{ product.stock > 0 ? `${product.stock}件` : '已售罄' }}
            </span>
          </div>
          
          <!-- 新鲜度信息 -->
          <template v-if="product.shelfLifeDays && product.productionDate">
            <div class="info-row">
              <span class="label">新鲜度</span>
              <div class="freshness-inline">
                <FreshnessBar 
                  :production-date="product.productionDate"
                  :shelf-life-days="product.shelfLifeDays"
                  :quality-grade="product.qualityGrade"
                  :storage-condition="product.storageCondition"
                  :show-meta="true"
                  :stroke-width="10"
                />
              </div>
            </div>
          </template>

          <!-- 存储条件标签 -->
          <div class="storage-badges" v-if="product.storageCondition || product.qualityGrade">
            <el-tag v-if="product.storageCondition" type="info" size="small">
              {{ product.storageCondition }}
            </el-tag>
            <el-tag v-if="product.qualityGrade" :type="product.qualityGrade === 'A' ? 'success' : product.qualityGrade === 'B' ? 'warning' : 'info'" size="small">
              {{ product.qualityGrade }}级品质
            </el-tag>
          </div>
          
          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              :disabled="product.stock <= 0"
            />
          </div>
          
          <!-- 操作按钮已移至页面底部固定栏 -->
          
          <!-- 服务保障 -->
          <div class="service-list">
            <div class="service-item">
              <el-icon><CircleCheck /></el-icon>
              正品保障
            </div>
            <div class="service-item">
              <el-icon><CircleCheck /></el-icon>
              新鲜直达
            </div>
            <div class="service-item">
              <el-icon><CircleCheck /></el-icon>
              7天无理由
            </div>
          </div>
          
        </div>
      </div>
      
      <!-- 商品描述 -->
      <div class="product-description-section">
        <el-tabs>
          <el-tab-pane label="商品详情">
            <div class="description-content">
              <p>{{ product.detail || product.description || product.subtitle || '暂无详细描述' }}</p>
              <div class="product-images" v-if="imageList.length > 0">
                <img
                  v-for="(img, index) in imageList"
                  :key="index"
                  :src="img"
                  :alt="`${product.name}-${index}`"
                  @error="handleImageError"
                />
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="'商品评价(' + reviewTotal + ')'">
            <div class="reviews-content">
              <template v-if="reviews.length > 0">
                <div class="review-item" v-for="review in reviews" :key="review.id">
                  <div class="review-header">
                    <el-avatar :size="40" :src="review.userAvatar">
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <div class="review-user-info">
                      <span class="username">{{ review.username || '匿名用户' }}</span>
                      <span class="review-time">{{ formatDateTime(review.createTime) }}</span>
                    </div>
                    <el-rate v-model="review.rating" disabled />
                  </div>
                  <div class="review-body">
                    <p class="review-text">{{ review.content }}</p>
                    <div class="review-images" v-if="review.images">
                      <el-image
                        v-for="(img, idx) in splitImageList(review.images)"
                        :key="idx"
                        :src="img"
                        :preview-src-list="splitImageList(review.images)"
                        fit="cover"
                        class="review-image"
                      />
                    </div>
                  </div>
                  <div class="review-reply" v-if="review.reply">
                    <span class="reply-label">商家回复：</span>
                    <span class="reply-text">{{ review.reply }}</span>
                  </div>
                </div>
                <el-pagination
                  v-if="reviewTotal > reviewPageSize"
                  layout="prev, pager, next"
                  :total="reviewTotal"
                  :page-size="reviewPageSize"
                  v-model:current-page="reviewPage"
                  @current-change="loadReviews"
                />
              </template>
              <el-empty v-else description="暂无评价" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="质量溯源">
            <div class="traceability-content">
              <template v-if="traceabilityList.length > 0">
                <el-timeline>
                  <el-timeline-item
                    v-for="item in traceabilityList"
                    :key="item.id"
                    :timestamp="formatDateTime(item.occurredTime)"
                    placement="top"
                    :color="getNodeColor(item.nodeType)"
                  >
                    <div class="trace-card">
                      <div class="trace-header">
                        <span class="trace-node-name">{{ item.nodeName }}</span>
                        <el-tag size="small" :type="getNodeTagType(item.nodeType)">
                          {{ getNodeTypeText(item.nodeType) }}
                        </el-tag>
                      </div>
                      <p v-if="item.description" class="trace-desc">{{ item.description }}</p>
                      <div class="trace-meta">
                        <span v-if="item.location" class="trace-meta-item">
                          <el-icon><Location /></el-icon> {{ item.location }}
                        </span>
                        <span v-if="item.operator" class="trace-meta-item">
                          <el-icon><User /></el-icon> {{ item.operator }}
                        </span>
                        <span v-if="item.temperature != null" class="trace-meta-item">
                          🌡️ {{ item.temperature }}℃
                        </span>
                        <span v-if="item.humidity != null" class="trace-meta-item">
                          💧 {{ item.humidity }}%
                        </span>
                      </div>
                      <el-image
                        v-if="item.imageUrl"
                        :src="normalizeImageUrl(item.imageUrl)"
                        fit="cover"
                        class="trace-image"
                        :preview-src-list="[normalizeImageUrl(item.imageUrl)]"
                      />
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </template>
              <el-empty v-else description="暂无溯源信息" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <!-- 固定底部操作栏 -->
    <div class="fixed-action-bar" v-if="product">
      <el-button
        type="primary"
        size="large"
        :disabled="product.stock <= 0"
        @click="handleAddCart"
      >
        <el-icon><ShoppingCart /></el-icon>
        加入购物车
      </el-button>
      <el-button
        type="warning"
        size="large"
        :disabled="product.stock <= 0"
        @click="handleBuyNow"
      >
        立即购买
      </el-button>
    </div>
    
    <el-empty v-else-if="!loading" description="商品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductDetail, getProductTraceability } from '@/api/product'
import { getProductReviews } from '@/api/review'
import { followShop, unfollowShop, checkFollowStatus } from '@/api/shop'
import type { Product, Review, ProductTraceability } from '@/types'
import { ElMessage } from 'element-plus'
import { Shop, Star, StarFilled, User, Location, ChatDotRound } from '@element-plus/icons-vue'
import FreshnessBar from '@/components/FreshnessBar.vue'
import { formatDateTime } from '@/utils/format'
import { normalizeImageUrl, splitImageList } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const product = ref<Product | null>(null)
const quantity = ref(1)
const isFollowed = ref(false)
const followLoading = ref(false)
const reviews = ref<Review[]>([])
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)
const traceabilityList = ref<ProductTraceability[]>([])

// 当前显示的图片
const currentImage = ref('')

const defaultProductImage = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" viewBox="0 0 600 600"><rect width="600" height="600" fill="#f0fdf4"/><text x="300" y="280" text-anchor="middle" font-size="120">🍊</text><text x="300" y="350" text-anchor="middle" fill="#94a3b8" font-size="28" font-family="sans-serif">暂无图片</text></svg>')

// 商品图片列表
const imageList = computed(() => {
  if (!product.value) return []
  const images = [
    normalizeImageUrl(product.value.mainImage),
    ...splitImageList(product.value.subImages),
    ...splitImageList(product.value.images)
  ].filter(Boolean) as string[]
  return Array.from(new Set(images))
})

const displayImage = computed(() => currentImage.value || imageList.value[0] || defaultProductImage)

function handleImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = defaultProductImage
}

function withTimeout<T>(promise: Promise<T>, timeout = 8000): Promise<T> {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) => {
      window.setTimeout(() => reject(new Error('request timeout')), timeout)
    })
  ])
}

// 加载商品详情
async function loadProduct() {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getProductDetail(id)
    product.value = res.data
    
    // 设置默认显示图片
    currentImage.value = imageList.value[0] || defaultProductImage
    
    void Promise.allSettled([
      userStore.isLoggedIn && product.value?.shopId
        ? withTimeout(checkFollowShopStatus())
        : Promise.resolve(),
      withTimeout(loadReviews()),
      withTimeout(loadTraceability())
    ])
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

// 加载溯源信息
async function loadTraceability() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res = await getProductTraceability(id)
    traceabilityList.value = res.data || []
  } catch {
    traceabilityList.value = []
  }
}

function getNodeTypeText(type: number): string {
  const map: Record<number, string> = { 1: '采摘', 2: '质检', 3: '入库', 4: '出库', 5: '配送' }
  return map[type] || '其他'
}

function getNodeColor(type: number): string {
  const map: Record<number, string> = { 1: '#10b981', 2: '#3b82f6', 3: '#8b5cf6', 4: '#f59e0b', 5: '#ef4444' }
  return map[type] || '#999'
}

function getNodeTagType(type: number): string {
  const map: Record<number, string> = { 1: 'success', 2: '', 3: 'warning', 4: 'warning', 5: 'danger' }
  return map[type] || 'info'
}

// 检查是否已关注店铺
async function checkFollowShopStatus() {
  if (!product.value?.shopId) return
  try {
    const res = await checkFollowStatus(product.value.shopId)
    isFollowed.value = res.data || false
  } catch {
    isFollowed.value = false
  }
}

// 加载评价列表
async function loadReviews() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res = await getProductReviews(id, reviewPage.value, reviewPageSize.value)
    if (res.data) {
      reviews.value = res.data.list || []
      reviewTotal.value = res.data.total || 0
    }
  } catch {
    reviews.value = []
  }
}

// 切换关注状态
async function handleToggleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!product.value?.shopId) return
  
  followLoading.value = true
  try {
    const res = isFollowed.value
      ? await unfollowShop(product.value.shopId)
      : await followShop(product.value.shopId)
    if (res.code === 200) {
      isFollowed.value = !isFollowed.value
      ElMessage.success(isFollowed.value ? '关注成功' : '取消关注成功')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

// 进入店铺
function visitShop() {
  if (!product.value?.shopId) return
  router.push(`/shop/${product.value.shopId}`)
}

// 联系客服
function contactService() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.value?.merchantId) return
  router.push(`/chat/${product.value.merchantId}`)
}

// 添加到购物车
async function handleAddCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!product.value) return
  
  const success = await cartStore.addToCart(product.value.id, quantity.value)
  if (success) {
    quantity.value = 1
  }
}

// 立即购买
async function handleBuyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!product.value) return
  
  // 直接跳转到结算页，通过 query 传递商品信息（不加入购物车）
  router.push({
    path: '/checkout',
    query: {
      buyNow: 'true',
      productId: product.value.id.toString(),
      quantity: quantity.value.toString()
    }
  })
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 80px;
}

/* 商品信息区 */
.product-info-section {
  display: flex;
  gap: 40px;
  background: white;
  border-radius: var(--radius-xl);
  padding: 32px;
}

/* 商品图片 */
.product-gallery {
  width: 480px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.thumbnail.active,
.thumbnail:hover {
  border-color: var(--color-primary);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品详情 */
.product-details {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

.product-desc {
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}

.price-section {
  background: linear-gradient(135deg, #fff8e6 0%, #fff3cd 100%);
  padding: 20px;
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-wrapper .label {
  color: var(--color-text-secondary);
  margin-right: 12px;
}

.price-wrapper .currency {
  color: var(--color-secondary);
  font-size: 18px;
  font-weight: 600;
}

.price-wrapper .price {
  color: var(--color-secondary);
  font-size: 36px;
  font-weight: 700;
}

.price-wrapper .unit {
  color: var(--color-text-light);
  font-size: 14px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row .label {
  width: 60px;
  color: var(--color-text-secondary);
}

.info-row .value {
  color: var(--color-text-primary);
}

.info-row .low-stock {
  color: var(--color-error);
}

.quantity-section {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.quantity-section .label {
  width: 60px;
  color: var(--color-text-secondary);
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.action-buttons .el-button {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

.fixed-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  padding: 12px 24px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 16px;
  justify-content: center;
}

.fixed-action-bar .el-button {
  flex: 1;
  max-width: 300px;
  height: 48px;
  font-size: 16px;
}

.service-list {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.service-item .el-icon {
  color: var(--color-primary);
}

/* 店铺信息卡片 */
.shop-info-card {
  margin-bottom: 20px;
  padding: 16px 20px;
  border: 1px solid #c8e6c9;
  border-radius: 12px;
  background: linear-gradient(135deg, #f0f9f0 0%, #e8f5e9 100%);
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.shop-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-text h4 {
  margin: 0 0 2px;
  font-size: 15px;
  font-weight: 600;
  color: #1b5e20;
}

.shop-text p {
  margin: 0;
  font-size: 12px;
  color: #66bb6a;
}

.shop-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 商品描述区 */
.product-description-section {
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
}

.description-content {
  padding: 20px 0;
  line-height: 1.8;
}

.product-images img {
  max-width: 100%;
  margin-top: 20px;
  border-radius: var(--radius-md);
}

.reviews-content {
  padding: 40px 0;
}

/* 评价列表 */
.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.review-user-info .username {
  font-weight: 500;
  color: var(--color-text-primary);
}

.review-user-info .review-time {
  font-size: 12px;
  color: var(--color-text-light);
  margin-top: 2px;
}

.review-text {
  color: var(--color-text-primary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.review-reply {
  margin-top: 12px;
  padding: 12px;
  background: #f8f8f8;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.reply-label {
  color: var(--color-primary);
  font-weight: 500;
}

.reply-text {
  color: var(--color-text-secondary);
}

.reviews-content :deep(.el-pagination) {
  margin-top: 20px;
  justify-content: center;
}

@media (max-width: 768px) {
  .product-info-section {
    flex-direction: column;
    padding: 20px;
  }
  
  .product-gallery {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.discount-tag {
  margin-left: 8px;
}

.freshness-inline {
  flex: 1;
}

.storage-badges {
  display: flex;
  gap: 8px;
  padding: 12px 0;
}

.traceability-content {
  padding: 20px 0;
}

.trace-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.trace-node-name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.trace-desc {
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.trace-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.trace-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.trace-image {
  width: 120px;
  height: 80px;
  border-radius: 6px;
  margin-top: 8px;
}
</style>
