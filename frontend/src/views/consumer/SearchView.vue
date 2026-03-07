<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果：{{ keyword }}</h2>
      <span class="result-count">共找到 {{ total }} 件商品</span>
    </div>
    
    <div class="product-grid" v-loading="loading">
      <template v-if="products.length > 0">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          @add-cart="handleAddCart"
        />
      </template>
      <el-empty v-else description="未找到相关商品" />
    </div>
    
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { searchProducts } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import type { Product } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const products = ref<Product[]>([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

watch(
  () => route.query.keyword,
  (newKeyword) => {
    keyword.value = String(newKeyword || '')
    pageNum.value = 1
    loadProducts()
  },
  { immediate: true }
)

async function loadProducts() {
  if (!keyword.value) return
  
  loading.value = true
  try {
    const res = await searchProducts(keyword.value, pageNum.value, pageSize.value)
    // 后端返回 list 而不是 records
    const data = res.data
    products.value = data?.list || data?.records || []
    total.value = data?.total || (data?.pages || 0) * pageSize.value
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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
.search-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.search-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.search-header h2 {
  font-size: 24px;
}

.result-count {
  color: var(--color-text-secondary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 300px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: var(--radius-md);
}
</style>
