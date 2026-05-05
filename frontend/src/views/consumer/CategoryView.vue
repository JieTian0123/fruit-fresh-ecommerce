<template>
  <div class="category-page">
    <!-- 左侧分类列表 -->
    <aside class="category-sidebar">
      <div class="sidebar-header">
        <el-icon><Grid /></el-icon>
        <span>全部分类</span>
      </div>
      <div class="category-list">
        <div
          class="category-item"
          :class="{ active: !currentCategoryId }"
          @click="selectCategory(null)"
        >
          全部商品
        </div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: currentCategoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </div>
      </div>
    </aside>
    
    <!-- 右侧商品列表 -->
    <main class="product-main">
      <div v-if="activityMeta" class="activity-hero" :class="`activity-${activityMeta.theme}`">
        <div>
          <span class="activity-badge">{{ activityMeta.badge }}</span>
          <h2>{{ activityMeta.title }}</h2>
          <p>{{ activityMeta.description }}</p>
        </div>
      </div>

      <!-- 筛选排序 -->
      <div class="filter-bar">
        <div class="sort-options">
          <span
            class="sort-item"
            :class="{ active: sortBy === '' }"
            @click="handleSort('')"
          >
            综合
          </span>
          <span
            class="sort-item"
            :class="{ active: sortBy === 'sales' }"
            @click="handleSort('sales')"
          >
            销量
            <el-icon v-if="sortBy === 'sales'">
              <CaretBottom v-if="sortOrder === 'desc'" />
              <CaretTop v-else />
            </el-icon>
          </span>
          <span
            class="sort-item"
            :class="{ active: sortBy === 'price' }"
            @click="handleSort('price')"
          >
            价格
            <el-icon v-if="sortBy === 'price'">
              <CaretBottom v-if="sortOrder === 'desc'" />
              <CaretTop v-else />
            </el-icon>
          </span>
          <span
            class="sort-item"
            :class="{ active: sortBy === 'createTime' }"
            @click="handleSort('createTime')"
          >
            最新
          </span>
        </div>
        
        <div class="price-filter">
          <el-input
            v-model.number="minPrice"
            placeholder="最低价"
            size="small"
            style="width: 80px"
          />
          <span>-</span>
          <el-input
            v-model.number="maxPrice"
            placeholder="最高价"
            size="small"
            style="width: 80px"
          />
          <el-button size="small" @click="handlePriceFilter">确定</el-button>
        </div>
      </div>
      
      <!-- 商品网格 -->
      <div class="product-grid" v-loading="loading">
        <template v-if="products.length > 0">
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
            @add-cart="handleAddCart"
          />
        </template>
        <el-empty v-else description="暂无商品" />
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getCategoryList, getProductList } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import type { Category, Product } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 数据
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const loading = ref(false)

// 筛选参数
const currentCategoryId = ref<number | null>(null)
const sortBy = ref('')
const sortOrder = ref<string>('desc')
const minPrice = ref<number>()
const maxPrice = ref<number>()
const keyword = ref('')

// 分页
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 监听路由参数
watch(
  () => [route.params.id, route.query.sort, route.query.activity, route.query.keyword],
  ([newId, querySort, activity, queryKeyword]) => {
    currentCategoryId.value = newId ? Number(newId) : null
    sortBy.value = typeof querySort === 'string' ? querySort : activity === 'daily-new' ? 'createTime' : sortBy.value
    keyword.value = typeof queryKeyword === 'string' ? queryKeyword : ''
    pageNum.value = 1
    loadProducts()
  },
  { immediate: true }
)

const activityMeta = computed(() => {
  const activity = route.query.activity
  if (activity === 'daily-new') {
    return {
      theme: 'daily',
      badge: '新鲜优选',
      title: '新鲜到店',
      description: '刚采摘入库生鲜'
    }
  }
  if (activity === 'seasonal') {
    return {
      theme: 'deal',
      badge: '动态降价',
      title: '临期特惠',
      description: '保质期过半低价购'
    }
  }
  return null
})

// 加载分类
async function loadCategories() {
  try {
    const res = await getCategoryList()
    categories.value = (res.data || [])
      .filter(cat => cat.status === undefined || cat.status === 1)
      .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  } catch {
    categories.value = []
  }
}

// 加载商品
async function loadProducts() {
  loading.value = true
  try {
    const res = await getProductList({
      categoryId: currentCategoryId.value || undefined,
      keyword: keyword.value || undefined,
      sortBy: sortBy.value || undefined,
      sortOrder: sortOrder.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    // 后端返回 list 而不是 records，pages 而不是 total
    const data = res.data
    products.value = data?.list || data?.records || []
    // 计算总条数：pages * pageSize 或直接使用 total
    total.value = data?.total || (data?.pages || 0) * pageSize.value
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 选择分类
function selectCategory(id: number | null) {
  if (id) {
    router.push(`/category/${id}`)
  } else {
    router.push('/category')
  }
}

// 排序
function handleSort(field: string) {
  if (sortBy.value === field) {
    // 切换排序方向
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  } else {
    sortBy.value = field
    sortOrder.value = 'desc'
  }
  pageNum.value = 1
  loadProducts()
}

// 价格筛选
function handlePriceFilter() {
  pageNum.value = 1
  loadProducts()
}

// 添加到购物车
async function handleAddCart(product: Product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await cartStore.addToCart(product.id, 1)
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-page {
  display: flex;
  gap: 24px;
}

/* 左侧分类 */
.category-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: white;
  border-radius: var(--radius-lg);
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 100px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 12px;
}

.sidebar-header .el-icon {
  color: var(--color-primary);
}

.category-item {
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 4px;
}

.category-item:hover {
  background: var(--color-background);
}

.category-item.active {
  background: var(--color-primary);
  color: white;
}

/* 右侧商品区 */
.product-main {
  flex: 1;
  min-width: 0;
}

.activity-hero {
  min-height: 132px;
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.activity-hero::after {
  content: '';
  position: absolute;
  right: 28px;
  bottom: -24px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
}

.activity-hero h2 {
  position: relative;
  z-index: 1;
  font-size: 26px;
  margin: 8px 0 6px;
  color: #244231;
}

.activity-hero p {
  position: relative;
  z-index: 1;
  color: #5d6d62;
  margin: 0;
}

.activity-badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.7);
  color: #2f7a4f;
}

.activity-daily {
  background: linear-gradient(135deg, #edfdf3 0%, #d9f5e5 55%, #fff1c8 100%);
}

.activity-seasonal {
  background: linear-gradient(135deg, #fff8dc 0%, #eef8e6 48%, #d8f0ee 100%);
}

.activity-deal {
  background: linear-gradient(135deg, #f0fdf4 0%, #fffbeb 58%, #fed7aa 100%);
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: var(--radius-md);
  padding: 16px 20px;
  margin-bottom: 20px;
}

.sort-options {
  display: flex;
  gap: 24px;
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.3s;
}

.sort-item:hover {
  color: var(--color-primary);
}

.sort-item.active {
  color: var(--color-primary);
  font-weight: 500;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px;
  background: white;
  border-radius: var(--radius-md);
}

@media (max-width: 768px) {
  .category-page {
    flex-direction: column;
  }
  
  .category-sidebar {
    width: 100%;
    position: static;
  }
  
  .category-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .category-item {
    flex: 0 0 auto;
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
