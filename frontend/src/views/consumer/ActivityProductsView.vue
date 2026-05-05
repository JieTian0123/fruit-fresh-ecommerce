<template>
  <div class="activity-page">
    <section class="activity-hero" :class="`theme-${activity.theme}`">
      <div class="hero-copy">
        <span class="hero-badge">{{ activity.badge }}</span>
        <h1>{{ activity.title }}</h1>
        <div class="hero-meta">
          <span>{{ activity.ruleLabel }}</span>
          <span>{{ total }} 件商品</span>
        </div>
      </div>
      <img :src="activity.imageUrl" :alt="activity.title" class="hero-image" />
    </section>

    <div class="activity-toolbar">
      <div class="toolbar-title">{{ activity.sectionTitle }}</div>
      <div class="toolbar-actions">
        <el-button
          v-for="option in sortOptions"
          :key="option.value"
          :type="sortBy === option.value ? 'primary' : 'default'"
          size="small"
          round
          @click="changeSort(option.value)"
        >
          {{ option.label }}
        </el-button>
      </div>
    </div>

    <div class="product-grid" v-loading="loading">
      <ProductCard
        v-for="product in activityProducts"
        :key="product.id"
        :product="product"
        @add-cart="handleAddCart"
      />
    </div>

    <el-empty v-if="!loading && activityProducts.length === 0" description="暂无活动商品" />

    <div class="load-more" v-if="activityProducts.length > 0">
      <el-button v-if="hasMore" :loading="loadingMore" round @click="loadMore">加载更多</el-button>
      <span v-else>已展示全部活动商品</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import { getProductList } from '@/api/product'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import type { Product, ProductQuery } from '@/types'

type ActivityCode = 'fresh-new' | 'near-expiry' | 'hot-sales'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const activityConfigs: Record<ActivityCode, {
  title: string
  badge: string
  ruleLabel: string
  sectionTitle: string
  imageUrl: string
  theme: string
  apiActivity: string
  defaultSort: string
}> = {
  'fresh-new': {
    title: '新鲜到店',
    badge: '新鲜优选',
    ruleLabel: '刚采摘/生产且设置保质期',
    sectionTitle: '新鲜到店生鲜',
    imageUrl: '/activity/fresh-new.svg',
    theme: 'daily',
    apiActivity: 'fresh-new',
    defaultSort: 'createTime'
  },
  'near-expiry': {
    title: '临期特惠',
    badge: '动态降价',
    ruleLabel: '保质期过半但仍在保',
    sectionTitle: '临期低价生鲜',
    imageUrl: '/activity/near-expiry.svg',
    theme: 'deal',
    apiActivity: 'near-expiry',
    defaultSort: 'freshness'
  },
  'hot-sales': {
    title: '热销榜单',
    badge: '人气热卖',
    ruleLabel: '按平台销量排序',
    sectionTitle: '热销生鲜',
    imageUrl: '/activity/hot-sales.svg',
    theme: 'hot',
    apiActivity: 'hot-sales',
    defaultSort: 'sales'
  }
}

const sortOptions = [
  { label: '新品', value: 'createTime' },
  { label: '热销', value: 'sales' },
  { label: '价格', value: 'price' },
  { label: '保质期', value: 'freshness' }
]

const loading = ref(false)
const loadingMore = ref(false)
const activityProducts = ref<Product[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 24
const hasMore = ref(true)
const sortBy = ref('createTime')

const activityCode = computed<ActivityCode>(() => {
  const code = route.params.code
  if (code === 'near-expiry' || code === 'hot-sales') return code
  if (code === 'daily-new') return 'fresh-new'
  if (code === 'seasonal-fresh' || code === 'fresh-deal') return 'near-expiry'
  return 'fresh-new'
})

const activity = computed(() => activityConfigs[activityCode.value])

watch(activityCode, () => {
  sortBy.value = activity.value.defaultSort
  resetAndLoad()
}, { immediate: true })

async function resetAndLoad() {
  pageNum.value = 1
  hasMore.value = true
  activityProducts.value = []
  await loadProducts(true)
}

function changeSort(nextSort: string) {
  sortBy.value = nextSort
  resetAndLoad()
}

async function loadProducts(isInit = false) {
  if (isInit) loading.value = true
  else loadingMore.value = true

  try {
    const params: ProductQuery = {
      pageNum: pageNum.value,
      pageSize,
      sortBy: sortBy.value,
      sortOrder: sortBy.value === 'price' ? 'asc' : 'desc',
      activity: activity.value.apiActivity
    }
    const res = await getProductList(params)
    const list = res.data?.list || res.data?.records || []

    if (isInit) {
      activityProducts.value = list
    } else {
      activityProducts.value.push(...list)
    }

    total.value = res.data?.total || activityProducts.value.length
    hasMore.value = list.length >= pageSize
    if (hasMore.value) {
      pageNum.value += 1
    }
  } catch {
    if (isInit) {
      activityProducts.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  await loadProducts(false)
}

async function handleAddCart(product: Product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await cartStore.addToCart(product.id, 1)
}
</script>

<style scoped>
.activity-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 10px 48px;
}

.activity-hero {
  min-height: 220px;
  border-radius: 16px;
  padding: 28px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  overflow: hidden;
  border: 1px solid rgba(16, 185, 129, 0.12);
}

.theme-daily {
  background: linear-gradient(135deg, #ecfdf5 0%, #dcfce7 55%, #fef3c7 100%);
}

.theme-seasonal {
  background: linear-gradient(135deg, #fff7ed 0%, #f0fdf4 52%, #ccfbf1 100%);
}

.theme-deal {
  background: linear-gradient(135deg, #f0fdf4 0%, #fffbeb 58%, #fed7aa 100%);
}

.theme-hot {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 48%, #dcfce7 100%);
}

.hero-copy {
  min-width: 240px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #047857;
  font-weight: 700;
  font-size: 13px;
}

.hero-copy h1 {
  margin: 16px 0 18px;
  font-size: 38px;
  color: #14532d;
  letter-spacing: 0;
}

.hero-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-meta span {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  color: #4b5563;
  font-size: 13px;
}

.hero-image {
  width: min(420px, 48%);
  max-height: 180px;
  object-fit: contain;
}

.activity-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 28px 0 18px;
}

.toolbar-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.load-more {
  margin-top: 32px;
  text-align: center;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .activity-hero {
    padding: 24px;
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-copy h1 {
    font-size: 30px;
  }

  .hero-image {
    width: 100%;
    max-height: 160px;
  }

  .activity-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
