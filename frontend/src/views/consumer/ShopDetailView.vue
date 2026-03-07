<template>
  <div class="shop-detail-page">
    <!-- 店铺信息头部 -->
    <div class="shop-header-card">
      <el-avatar :size="72" :src="shopInfo?.logo ? '/api' + shopInfo.logo : undefined">
        <el-icon :size="32"><Shop /></el-icon>
      </el-avatar>
      <div class="shop-info">
        <h2>{{ shopInfo?.shopName || '加载中...' }}</h2>
        <p class="shop-desc">{{ shopInfo?.description || '暂无简介' }}</p>
        <div class="shop-meta">
          <span v-if="shopInfo?.province">
            <el-icon><Location /></el-icon>
            {{ shopInfo.province }}{{ shopInfo.city }}{{ shopInfo.district }}
          </span>
          <span v-if="shopInfo?.contactPhone">
            <el-icon><Phone /></el-icon>
            {{ shopInfo.contactPhone }}
          </span>
        </div>
      </div>
      <div class="shop-actions">
        <el-button
          :type="isFollowed ? 'default' : 'primary'"
          @click="handleToggleFollow"
          :loading="followLoading"
        >
          {{ isFollowed ? '已关注' : '关注店铺' }}
        </el-button>
        <el-button type="info" plain @click="contactService">
          <el-icon><ChatDotRound /></el-icon>
          联系客服
        </el-button>
      </div>
    </div>

    <!-- 店铺商品列表 -->
    <div class="shop-products-section">
      <h3>店铺商品</h3>
      <div class="product-grid" v-loading="loading">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          @add-cart="handleAddCart"
        />
      </div>
      <el-empty v-if="!loading && products.length === 0" description="店铺暂无商品" />
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadProducts"
          @size-change="loadProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getShopDetail, getShopProducts, followShop, unfollowShop, checkFollowStatus } from '@/api/shop'
import ProductCard from '@/components/ProductCard.vue'
import type { Product } from '@/types'
import { ElMessage } from 'element-plus'
import { Shop, Location, Phone, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const shopInfo = ref<any>(null)
const products = ref<Product[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const isFollowed = ref(false)
const followLoading = ref(false)

const shopId = Number(route.params.id)

async function loadShopInfo() {
  try {
    const res = await getShopDetail(shopId)
    if (res.code === 200) {
      shopInfo.value = res.data
    }
  } catch {
    ElMessage.error('加载店铺信息失败')
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await getShopProducts(shopId, { pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      products.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

async function checkFollow() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await checkFollowStatus(shopId)
    isFollowed.value = res.data || false
  } catch {
    isFollowed.value = false
  }
}

async function handleToggleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  followLoading.value = true
  try {
    const res = isFollowed.value
      ? await unfollowShop(shopId)
      : await followShop(shopId)
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

async function handleAddCart(product: Product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await cartStore.addToCart(product.id, 1)
}

function contactService() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push({ path: '/messages', query: { merchantId: shopInfo.value?.merchantId } })
}

onMounted(() => {
  if (shopId) {
    loadShopInfo()
    loadProducts()
    checkFollow()
  }
})
</script>

<style scoped>
.shop-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.shop-header-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.shop-info {
  flex: 1;
}

.shop-info h2 {
  font-size: 22px;
  margin: 0 0 8px 0;
  color: #333;
}

.shop-desc {
  color: #666;
  font-size: 14px;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.shop-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.shop-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.shop-meta .el-icon {
  font-size: 14px;
}

.shop-actions {
  flex-shrink: 0;
}

.shop-products-section h3 {
  font-size: 18px;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #4caf50;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .shop-header-card {
    flex-direction: column;
    text-align: center;
  }

  .shop-meta {
    justify-content: center;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
