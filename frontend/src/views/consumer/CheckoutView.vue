<template>
  <div class="checkout-page" v-loading="loading">
    <!-- 收货地址 -->
    <section class="section address-section">
      <h3 class="section-title">
        <el-icon><Location /></el-icon>
        收货地址
      </h3>
      
      <div v-if="selectedAddress" class="selected-address">
        <div class="address-info">
          <span class="receiver">{{ selectedAddress.receiverName }}</span>
          <span class="phone">{{ maskPhone(selectedAddress.receiverPhone) }}</span>
          <el-tag v-if="selectedAddress.isDefault" size="small" type="success">默认</el-tag>
        </div>
        <p class="address-detail">
          {{ getMaskedAddressText(selectedAddress) }}
        </p>
        <el-button type="primary" link @click="showAddressDialog = true">
          更换地址
        </el-button>
      </div>
      
      <el-button v-else type="primary" @click="showAddressDialog = true">
        <el-icon><Plus /></el-icon>
        添加收货地址
      </el-button>
    </section>
    
    <!-- 商品清单 -->
    <section class="section products-section">
      <h3 class="section-title">
        <el-icon><Goods /></el-icon>
        商品清单
      </h3>
      
      <div class="product-list">
        <div v-for="item in selectedItems" :key="item.id" class="product-item">
          <img
            :src="getCheckoutProductImage(item)"
            :alt="item.product?.name || item.productName"
            class="product-image"
          />
          <div class="product-info">
            <h4>{{ item.product?.name || item.productName }}</h4>
            <p class="unit">{{ item.product?.unit || '500g' }}</p>
          </div>
          <div class="product-price">
            <template v-if="isBuyNow">
              <span class="price">¥{{ formatMoney(getItemPrice(item)) }}</span>
              <el-input-number 
                v-model="buyNowQuantity" 
                :min="1" 
                :max="item.product?.stock || 99" 
                size="small" 
                style="width: 100px; margin-left: 10px;"
              />
            </template>
            <template v-else>
              <span class="price">¥{{ formatMoney(getItemPrice(item)) }}</span>
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.product?.stock || 99"
                size="small"
                style="width: 100px; margin-left: 10px;"
                @change="(val: number) => updateCartQuantity(item.id, val)"
              />
            </template>
          </div>
          <div class="product-subtotal">
             ¥{{ formatMoney(getItemPrice(item) * item.quantity) }}
          </div>
        </div>
      </div>
    </section>
    
    <!-- 订单备注 -->
    <section class="section remark-section">
      <h3 class="section-title">订单备注</h3>
      <el-input
        v-model="remark"
        type="textarea"
        placeholder="选填，可以告诉商家您的特殊需求"
        :rows="2"
        maxlength="200"
        show-word-limit
      />
    </section>

    <!-- 优惠券 -->
    <section class="section coupon-section">
      <h3 class="section-title">
        <el-icon><Ticket /></el-icon>
        优惠券
      </h3>
      <div v-if="usableCoupons.length > 0">
        <div class="coupon-summary" @click="showCouponPicker = !showCouponPicker">
          <span v-if="selectedCouponId">已选择 1 张优惠券</span>
          <span v-else>{{ usableCoupons.length }} 张可用</span>
          <el-button type="primary" link>{{ showCouponPicker ? '收起' : '选择' }}</el-button>
        </div>
        <div v-show="showCouponPicker" class="coupon-list">
          <div
            v-for="coupon in usableCoupons"
            :key="coupon.id"
            class="coupon-item"
            :class="{ selected: selectedCouponId === coupon.id }"
            @click="toggleCoupon(coupon.id)"
          >
            <div class="coupon-info">
              <p class="coupon-title">{{ coupon.title || '优惠券' }}</p>
              <p class="coupon-desc">
                <template v-if="coupon.couponType === 2">
                  {{ (coupon.discountRate * 10).toFixed(1) }}折
                </template>
                <template v-else>
                  减¥{{ coupon.discountAmount }}
                </template>
                <span v-if="coupon.minimumAmount"> | 满{{ coupon.minimumAmount }}可用</span>
              </p>
              <p class="coupon-expire">有效期至 {{ coupon.validUntil?.substring(0, 10) }}</p>
            </div>
            <el-icon v-if="selectedCouponId === coupon.id" class="check-icon"><CircleCheck /></el-icon>
          </div>
          <div
            class="coupon-item no-coupon"
            :class="{ selected: selectedCouponId === null }"
            @click="selectedCouponId = null"
          >
            <span>不使用优惠券</span>
          </div>
        </div>
      </div>
      <p v-else class="no-coupon-text">暂无可用优惠券</p>
    </section>
    
    <!-- 结算栏 -->
    <div class="checkout-footer">
      <div class="footer-info">
        <span>共 {{ selectedItems.length }} 件商品，</span>
        <span v-if="selectedCouponId" class="discount-info">已使用优惠券，</span>
        <span>合计：</span>
        <span v-if="couponDiscountAmount > 0" class="discount-text">已优惠 ¥{{ couponDiscountAmount.toFixed(2) }}，</span>
        <span class="total-amount">¥{{ payableAmount.toFixed(2) }}</span>
      </div>
      <el-button
        type="primary"
        size="large"
        :loading="submitting"
        :disabled="!selectedAddress || selectedItems.length === 0"
        @click="handleSubmitOrder"
      >
        提交订单
      </el-button>
    </div>
    
    <!-- 地址选择弹窗 -->
    <el-dialog v-model="showAddressDialog" title="选择收货地址" width="600px">
      <div class="address-list">
        <div
          v-for="addr in addressList"
          :key="addr.id"
          class="address-option"
          :class="{ active: selectedAddress?.id === addr.id }"
          @click="selectAddress(addr)"
        >
          <div class="address-info">
            <span class="receiver">{{ addr.receiverName }}</span>
            <span class="phone">{{ maskPhone(addr.receiverPhone) }}</span>
            <el-tag v-if="addr.isDefault" size="small" type="success">默认</el-tag>
          </div>
          <p class="address-detail">
            {{ getMaskedAddressText(addr) }}
          </p>
        </div>
        <el-empty v-if="addressList.length === 0" description="暂无收货地址" />
      </div>
      <template #footer>
        <el-button @click="showNewAddressForm = true">新建地址</el-button>
        <el-button @click="router.push('/address')">管理地址</el-button>
        <el-button type="primary" @click="showAddressDialog = false">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 新建地址对话框 -->
    <el-dialog v-model="showNewAddressForm" title="新建收货地址" width="500px">
      <el-form :model="newAddressForm" label-width="80px">
        <el-form-item label="收货人" required>
          <el-input v-model="newAddressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="newAddressForm.receiverPhone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="newAddressRegion"
            :options="regionData"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleNewAddressRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="newAddressForm.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="newAddressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewAddressForm = false">取消</el-button>
        <el-button type="primary" @click="handleSaveNewAddress" :loading="savingAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getAddressList, addAddress } from '@/api/address'
import { getProductDetail } from '@/api/product'
import { createOrder } from '@/api/order'
import { getUsableCoupons } from '@/api/coupon'
import type { Address, Product } from '@/types'
import { ElMessage } from 'element-plus'
import { regionData, codeToText } from 'element-china-area-data'
import { defaultImage, getProductImage, normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const submitting = ref(false)
const showAddressDialog = ref(false)
const showNewAddressForm = ref(false)
const savingAddress = ref(false)
const addressList = ref<Address[]>([])
const selectedAddress = ref<Address | null>(null)
const remark = ref('')
const usableCoupons = ref<any[]>([])
const selectedCouponId = ref<number | null>(null)
const showCouponPicker = ref(false)

// 新建地址表单
const newAddressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

const newAddressRegion = ref<string[]>([])

function maskPhone(phone: string) {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function getMaskedAddressText(address: Address) {
  return [address.province, address.city, address.district].filter(Boolean).join(' ')
}

function handleNewAddressRegionChange(value: string[]) {
  if (value && value.length === 3) {
    newAddressForm.value.province = (codeToText as Record<string, string>)[value[0]!] || ''
    newAddressForm.value.city = (codeToText as Record<string, string>)[value[1]!] || ''
    newAddressForm.value.district = (codeToText as Record<string, string>)[value[2]!] || ''
  } else {
    newAddressForm.value.province = ''
    newAddressForm.value.city = ''
    newAddressForm.value.district = ''
  }
}

// 立即购买模式
const isBuyNow = computed(() => route.query.buyNow === 'true')
const buyNowProduct = ref<Product | null>(null)
const buyNowQuantity = ref(1)

// 商品列表（兼容购物车和立即购买）
const selectedItems = computed(() => {
  if (isBuyNow.value && buyNowProduct.value) {
    return [{
      id: 0,
      userId: 0,
      productId: buyNowProduct.value.id,
      quantity: buyNowQuantity.value,
      selected: 1,
      product: buyNowProduct.value,
      // 添加扁平字段以满足 CartItem 类型
      productName: buyNowProduct.value.name,
      productImage: getProductImage(buyNowProduct.value),
      price: buyNowProduct.value.price,
      stock: buyNowProduct.value.stock
    }]
  }
  return cartStore.selectedItems
})

const totalAmount = computed(() => {
  if (isBuyNow.value && buyNowProduct.value) {
    return toNumber(buyNowProduct.value.price) * buyNowQuantity.value
  }
  return cartStore.totalAmount
})

const selectedCoupon = computed(() => {
  if (!selectedCouponId.value) {
    return null
  }
  return usableCoupons.value.find(coupon => coupon.id === selectedCouponId.value) || null
})

const couponDiscountAmount = computed(() => {
  const coupon = selectedCoupon.value
  const rawTotal = totalAmount.value

  if (!coupon || rawTotal <= 0) {
    return 0
  }

  if (coupon.couponType === 2) {
    const discountRate = Number(coupon.discountRate || 0)
    let discount = rawTotal - rawTotal * discountRate
    const maxDiscount = Number(coupon.maximumDiscount || 0)
    if (maxDiscount > 0) {
      discount = Math.min(discount, maxDiscount)
    }
    return Math.max(0, Math.min(discount, rawTotal))
  }

  const discountAmount = Number(coupon.discountAmount || 0)
  return Math.max(0, Math.min(discountAmount, rawTotal))
})

const payableAmount = computed(() => {
  return Math.max(0, totalAmount.value - couponDiscountAmount.value)
})

function toNumber(value: unknown) {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function formatMoney(value: unknown) {
  return toNumber(value).toFixed(2)
}

function getItemPrice(item: any) {
  return toNumber(item.product?.price ?? item.price)
}

function getCheckoutProductImage(item: any) {
  if (item.product) {
    return getProductImage(item.product)
  }
  return normalizeImageUrl(item.productImage, defaultImage)
}

async function loadAddresses() {
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
    
    // 默认选中默认地址或第一个地址
    const defaultAddr = addressList.value.find(a => a.isDefault)
    selectedAddress.value = defaultAddr || addressList.value[0] || null
  } catch {
    addressList.value = []
  }
}

function selectAddress(addr: Address) {
  selectedAddress.value = addr
  showAddressDialog.value = false
}

function toggleCoupon(id: number) {
  if (selectedCouponId.value === id) {
    selectedCouponId.value = null
  } else {
    selectedCouponId.value = id
  }
}

async function updateCartQuantity(id: number, quantity: number) {
  await cartStore.updateQuantity(id, quantity)
}

async function loadUsableCoupons() {
  try {
    const amount = totalAmount.value
    if (amount <= 0) {
      usableCoupons.value = []
      selectedCouponId.value = null
      return
    }
    const res = await getUsableCoupons(amount)
    usableCoupons.value = res.data || []

    if (selectedCouponId.value) {
      const selectedCoupon = usableCoupons.value.find(coupon => coupon.id === selectedCouponId.value)
      if (!selectedCoupon) {
        selectedCouponId.value = null
        ElMessage.info('当前优惠券不满足使用门槛，已自动取消')
      }
    }
  } catch {
    usableCoupons.value = []
  }
}

watch(totalAmount, async (amount, previousAmount) => {
  if (amount === previousAmount) {
    return
  }
  await loadUsableCoupons()
})
// 保存新建地址
async function handleSaveNewAddress() {
  const form = newAddressForm.value
  if (!form.receiverName || !form.receiverPhone || !form.province || !form.detailAddress) {
    ElMessage.warning('请填写完整的收货地址信息')
    return
  }
  
  savingAddress.value = true
  try {
    await addAddress({
      ...form,
      isDefault: form.isDefault ? 1 : 0
    } as any)
    ElMessage.success('地址添加成功')
    showNewAddressForm.value = false
    // 重置表单
    newAddressForm.value = {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
    newAddressRegion.value = []
    // 刷新地址列表
    await loadAddresses()
  } catch {
    // 错误已处理
  } finally {
    savingAddress.value = false
  }
}

async function handleSubmitOrder() {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  submitting.value = true
  try {
    // 构建订单数据
    const orderData: any = {
      addressId: selectedAddress.value.id,
      remark: remark.value
    }
    if (selectedCouponId.value) {
      orderData.couponId = selectedCouponId.value
    }
    
    // 立即购买模式：传递 productId 和 quantity
    if (isBuyNow.value && buyNowProduct.value) {
      orderData.productId = buyNowProduct.value.id
      orderData.quantity = buyNowQuantity.value
    } else {
      // 购物车模式：传递 cartItemIds
      orderData.cartItemIds = selectedItems.value.map(item => item.id)
    }
    
    const res = await createOrder(orderData)
    
    ElMessage.success('订单提交成功')
    
    // 非立即购买模式时，清除购物车中已选商品
    if (!isBuyNow.value) {
      cartStore.removeSelectedItems()
    }
    
    // 跳转到订单详情或支付页面
    const orderNo = res.data
    if (orderNo) {
      router.push(`/order/${orderNo}`)
    } else {
      router.push('/orders')
    }
  } catch {
    // 错误已处理
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadAddresses()
  
  // 如果是立即购买模式，加载商品信息
  if (isBuyNow.value) {
    if (route.query.productId) {
      // buyNowQuantity 默认取 query 或 1
      buyNowQuantity.value = Number(route.query.quantity) || 1
      // 获取商品详情 ...
      const productId = Number(route.query.productId)
      try {
        const productRes = await getProductDetail(productId)
        buyNowProduct.value = productRes.data
      } catch {
        ElMessage.error('商品信息加载失败')
        router.push('/')
      }
    } else {
      ElMessage.error('商品ID缺失')
      router.push('/')
    }
  } else {
    // 购物车模式，检查是否有选中商品
    if (cartStore.selectedItems.length === 0) {
      ElMessage.warning('请先选择要结算的商品')
      router.push('/cart')
    }
  }
  loading.value = false
  await loadUsableCoupons()
})
</script>

<style scoped>
.checkout-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 100px;
}

.section {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  margin-bottom: 20px;
}

.section-title .el-icon {
  color: var(--color-primary);
}

/* 地址 */
.selected-address {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--color-background);
  border-radius: var(--radius-md);
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.receiver {
  font-weight: 600;
}

.address-detail {
  flex: 1 0 100%;
  color: var(--color-text-secondary);
  margin-top: 8px;
}

/* 商品清单 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--color-background);
  border-radius: var(--radius-md);
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 4px;
}

.product-info .unit {
  color: var(--color-text-light);
  font-size: 13px;
}

.product-price {
  color: var(--color-text-secondary);
}

.product-subtotal {
  font-weight: 600;
  color: var(--color-secondary);
  min-width: 100px;
  text-align: right;
}

/* 结算栏 */
.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-secondary);
}

/* 地址选择 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.address-option {
  padding: 16px;
  border: 2px solid #eee;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s;
}

.address-option:hover {
  border-color: var(--color-primary-light);
}

.address-option.active {
  border-color: var(--color-primary);
  background: #f0fff0;
}

/* 优惠券 */
.coupon-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--color-background);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.coupon-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 2px solid #eee;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item:hover {
  border-color: var(--color-primary-light);
}

.coupon-item.selected {
  border-color: var(--color-primary);
  background: #f0fff0;
}

.coupon-item.no-coupon {
  justify-content: center;
  color: var(--color-text-secondary);
  padding: 12px;
}

.coupon-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.coupon-desc {
  font-size: 13px;
  color: var(--color-secondary);
  margin-bottom: 4px;
}

.coupon-expire {
  font-size: 12px;
  color: var(--color-text-light);
}

.check-icon {
  color: var(--color-primary);
  font-size: 24px;
}

.no-coupon-text {
  color: var(--color-text-light);
  font-size: 14px;
}

.discount-info {
  color: var(--color-secondary);
  font-weight: 500;
}

.discount-text {
  color: var(--color-secondary);
  font-weight: 500;
}
</style>
