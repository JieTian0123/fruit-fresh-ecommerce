<template>
  <div class="cart-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><ShoppingCart /></el-icon>
        购物车
      </h1>
    </div>
    
    <div class="cart-content" v-loading="loading">
      <template v-if="cartItems.length > 0">
        <!-- 购物车列表 -->
        <div class="cart-list">
          <!-- 全选 -->
          <div class="cart-header">
            <el-checkbox v-model="isAllChecked" @change="handleSelectAll">
              全选
            </el-checkbox>
            <span class="header-item product">商品信息</span>
            <span class="header-item price">单价</span>
            <span class="header-item quantity">数量</span>
            <span class="header-item subtotal">小计</span>
            <span class="header-item action">操作</span>
          </div>
          
          <!-- 商品项 -->
          <div
            v-for="item in cartItems"
            :key="item.id"
            class="cart-item"
          >
            <el-checkbox
              :model-value="item.selected === 1"
              @change="handleSelectItem(item)"
            />
            
            <div class="product-info" @click="goToProduct(item.productId)">
              <img
                :src="item.productImage || defaultImage"
                :alt="item.productName"
                class="product-image"
              />
              <div class="product-detail">
                <h3 class="product-name">{{ item.productName }}</h3>
                <p class="product-unit">500g</p>
              </div>
            </div>
            
            <div class="price">
              <span class="currency">¥</span>
              {{ item.price?.toFixed(2) }}
            </div>
            
            <div class="quantity">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stock || 99"
                size="small"
                @change="handleQuantityChange(item)"
              />
            </div>
            
            <div class="subtotal">
              <span class="currency">¥</span>
              {{ ((item.price || 0) * item.quantity).toFixed(2) }}
            </div>
            
            <div class="action">
              <el-button
                type="danger"
                link
                @click="handleDelete(item.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox v-model="isAllChecked" @change="handleSelectAll">
              全选
            </el-checkbox>
            <el-button type="danger" link @click="handleDeleteSelected">
              删除选中
            </el-button>
          </div>
          
          <div class="footer-right">
            <div class="selected-info">
              已选择 <span class="count">{{ selectedCount }}</span> 件商品
            </div>
            <div class="total-amount">
              合计：
              <span class="currency">¥</span>
              <span class="amount">{{ totalAmount.toFixed(2) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              :disabled="selectedCount === 0"
              @click="handleCheckout"
            >
              去结算（{{ selectedCount }}）
            </el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="购物车是空的">
        <el-button type="primary" @click="router.push('/')">
          去逛逛
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import type { CartItem } from '@/types'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const defaultImage = 'https://loremflickr.com/100/100/fruit,fresh'

// 计算属性
const loading = computed(() => cartStore.loading)
const cartItems = computed(() => cartStore.cartItems)
const selectedCount = computed(() => cartStore.selectedCount)
const totalAmount = computed(() => cartStore.totalAmount)
const isAllChecked = computed({
  get: () => cartStore.isAllSelected,
  set: () => {}
})

// 跳转商品详情
function goToProduct(productId: number) {
  router.push(`/product/${productId}`)
}

// 全选/取消全选
function handleSelectAll() {
  cartStore.toggleSelectAll()
}

// 选中单个商品
function handleSelectItem(item: CartItem) {
  cartStore.toggleSelect(item.id)
}

// 修改数量
function handleQuantityChange(item: CartItem) {
  cartStore.updateQuantity(item.id, item.quantity)
}

// 删除商品
async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除这件商品吗？', '提示', {
    type: 'warning'
  })
  cartStore.removeItem(id)
}

// 删除选中商品
async function handleDeleteSelected() {
  const selected = cartItems.value.filter(item => item.selected)
  if (selected.length === 0) return
  
  await ElMessageBox.confirm(`确定要删除选中的 ${selected.length} 件商品吗？`, '提示', {
    type: 'warning'
  })
  
  for (const item of selected) {
    await cartStore.removeItem(item.id)
  }
}

// 去结算
function handleCheckout() {
  if (selectedCount.value === 0) return
  router.push('/checkout')
}

onMounted(() => {
  cartStore.fetchCartList()
})
</script>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
}

.page-title .el-icon {
  color: var(--color-primary);
}

/* 购物车列表 */
.cart-list {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.cart-header .el-checkbox {
  margin-right: 20px;
}

.header-item {
  text-align: center;
}

.header-item.product {
  flex: 1;
  text-align: left;
  padding-left: 20px;
}

.header-item.price,
.header-item.quantity,
.header-item.subtotal {
  width: 120px;
}

.header-item.action {
  width: 80px;
}

/* 商品项 */
.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item .el-checkbox {
  margin-right: 20px;
}

.product-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--color-text-primary);
}

.product-name:hover {
  color: var(--color-primary);
}

.product-unit {
  font-size: 13px;
  color: var(--color-text-light);
}

.price,
.quantity,
.subtotal {
  width: 120px;
  text-align: center;
}

.price .currency,
.subtotal .currency {
  font-size: 12px;
}

.subtotal {
  font-weight: 600;
  color: var(--color-secondary);
}

.action {
  width: 80px;
  text-align: center;
}

/* 结算栏 */
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  position: sticky;
  bottom: 0;
  box-shadow: var(--shadow-md);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.selected-info {
  color: var(--color-text-secondary);
}

.selected-info .count {
  color: var(--color-primary);
  font-weight: 600;
}

.total-amount {
  font-size: 14px;
}

.total-amount .currency {
  color: var(--color-secondary);
  font-size: 14px;
}

.total-amount .amount {
  color: var(--color-secondary);
  font-size: 24px;
  font-weight: 700;
}

@media (max-width: 768px) {
  .cart-header {
    display: none;
  }
  
  .cart-item {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .product-info {
    width: 100%;
    order: 1;
  }
  
  .cart-item .el-checkbox {
    order: 0;
    margin-right: 0;
  }
  
  .price,
  .quantity,
  .subtotal,
  .action {
    width: auto;
    flex: 1;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
