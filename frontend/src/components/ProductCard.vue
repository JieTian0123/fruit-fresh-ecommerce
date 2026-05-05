<template>
  <div class="product-card" @click="goToDetail">
    <!-- 商品图片 -->
    <div class="product-image-wrapper">
      <img
        :src="productImage"
        :alt="product.name"
        class="product-image"
        @error="handleImageError"
      />
      <div v-if="product.stock <= 0" class="sold-out-mask">
        <span>已售罄</span>
      </div>
      <div v-else-if="product.stock <= 10" class="low-stock-badge">
        仅剩{{ product.stock }}件
      </div>
      <div v-if="product.discountLabel" class="discount-badge">
        {{ product.discountLabel }}
      </div>
    </div>
    
    <!-- 商品信息 -->
    <div class="product-info">
      <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
      
      <div class="product-meta">
        <span v-if="product.sales" class="sales">已售{{ formatSales(product.sales) }}</span>
        <span class="unit">{{ product.unit || '500g' }}</span>
        <span v-if="remainingDays >= 0" class="freshness-tag" :style="{ color: freshnessColor }">
          {{ remainingDays > 0 ? `剩${remainingDays}天` : '已过期' }}
        </span>
      </div>
      
      <div class="product-bottom">
        <div class="price-wrapper">
          <span class="currency">¥</span>
          <span class="price">{{ displayPrice }}</span>
          <span v-if="hasDiscount" class="original-price">¥{{ product.price?.toFixed(2) }}</span>
        </div>
        
        <el-button
          v-if="product.stock > 0"
          type="primary"
          size="small"
          circle
          @click.stop="handleAddCart"
        >
          <el-icon><ShoppingCart /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Product } from '@/types'
import { defaultImage, getProductImage } from '@/utils/image'

const props = defineProps<{
  product: Product
}>()

const emit = defineEmits<{
  (e: 'add-cart', product: Product): void
}>()

const router = useRouter()

const remainingDays = computed(() => {
  if (!props.product.productionDate || !props.product.shelfLifeDays) return -1
  const production = new Date(props.product.productionDate)
  const now = new Date()
  const daysPassed = Math.floor((now.getTime() - production.getTime()) / (1000 * 60 * 60 * 24))
  return Math.max(0, props.product.shelfLifeDays - daysPassed)
})

const freshnessColor = computed(() => {
  if (!props.product.shelfLifeDays) return '#999'
  const percent = (remainingDays.value / props.product.shelfLifeDays) * 100
  if (percent > 70) return '#10b981'
  if (percent > 30) return '#f59e0b'
  return '#ef4444'
})

const displayPrice = computed(() => {
  const price = props.product.currentPrice ?? props.product.price
  return price?.toFixed(2)
})

const hasDiscount = computed(() => {
  return props.product.currentPrice != null && 
         props.product.price != null &&
         props.product.currentPrice < props.product.price
})

const productImage = computed(() => getProductImage(props.product))

// 格式化销量
function formatSales(sales: number): string {
  if (sales >= 10000) {
    return (sales / 10000).toFixed(1) + '万'
  } else if (sales >= 1000) {
    return (sales / 1000).toFixed(1) + 'k'
  }
  return String(sales)
}

// 图片加载失败
function handleImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = defaultImage
}

// 跳转详情
function goToDetail() {
  router.push(`/product/${props.product.id}`)
}

// 添加到购物车
function handleAddCart() {
  emit('add-cart', props.product)
}
</script>

<style scoped>
.product-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  position: relative;
  border: 1px solid rgba(0,0,0,0.03);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* Square image for Fresh look */
  overflow: hidden;
  background-color: #f9fafb;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.08);
}

.sold-out-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 16px;
  font-weight: 600;
  backdrop-filter: blur(2px);
}

.low-stock-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-warning);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(4px);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--color-text-light);
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-wrapper {
  color: var(--color-text-primary);
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.currency {
  font-size: 14px;
}

.price {
  font-size: 20px;
}

/* Custom Add Button */
.el-button--circle {
  width: 36px;
  height: 36px;
  border: none;
  background-color: #F3F4F6;
  color: var(--color-text-secondary);
  transition: all 0.3s;
}

.product-card:hover .el-button--circle {
  background-color: var(--color-primary);
  color: white;
  transform: scale(1.1);
}

.discount-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  z-index: 2;
}

.freshness-tag {
  font-size: 11px;
  font-weight: 500;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 4px;
}
</style>
