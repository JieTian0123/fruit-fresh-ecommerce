<template>
  <div class="order-detail-page" v-loading="loading">
    <template v-if="order">
      <!-- 订单状态 -->
      <div class="status-card">
        <div class="status-info">
          <el-icon class="status-icon" :style="{ color: getStatusColor(order.status) }">
            <component :is="getStatusIcon(order.status)" />
          </el-icon>
          <div>
            <h2>{{ getStatusText(order.status) }}</h2>
            <p class="status-desc">{{ getStatusDesc(order.status) }}</p>
          </div>
        </div>
        <div class="status-actions">
          <el-button v-if="order.status === 0" type="danger" @click="handleCancel">取消订单</el-button>
          <el-button v-if="order.status === 0" type="primary" @click="handlePay">立即付款</el-button>
          <el-button v-if="order.status === 2" type="primary" @click="handleReceive">确认收货</el-button>
        </div>
      </div>
      
      <!-- 收货信息 -->
      <div class="section">
        <h3><el-icon><Location /></el-icon> 收货信息</h3>
        <div class="address-info">
          <p><strong>{{ order.receiverName }}</strong> {{ order.receiverPhone }}</p>
          <p>{{ order.receiverAddress }}</p>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="section">
        <h3><el-icon><Goods /></el-icon> 商品信息</h3>
        <div class="product-list">
          <div v-for="item in order.orderItems" :key="item.id" class="product-item">
            <img :src="normalizeImageUrl(item.productImage, defaultImage)" :alt="item.productName" class="product-image" />
            <div class="product-info">
              <h4>{{ item.productName }}</h4>
              <p class="price">¥{{ formatMoney(item.price) }} × {{ item.quantity }}</p>
            </div>
            <div class="subtotal">¥{{ formatMoney(getItemSubtotal(item)) }}</div>
            <el-button
              v-if="order.status === 3"
              :type="item.reviewed ? 'info' : 'primary'"
              :disabled="item.reviewed"
              size="small"
              @click="openReviewDialog(item)"
            >
              {{ item.reviewed ? '已评价' : '写评价' }}
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 订单信息 -->
      <div class="section">
        <h3><el-icon><Document /></el-icon> 订单信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">订单编号</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间</span>
            <span class="value">{{ formatDateTime(order.createTime) }}</span>
          </div>
          <div class="info-item" v-if="order.payTime">
            <span class="label">支付时间</span>
            <span class="value">{{ formatDateTime(order.payTime) }}</span>
          </div>
          <div class="info-item" v-if="order.remark">
            <span class="label">订单备注</span>
            <span class="value">{{ order.remark }}</span>
          </div>
          <div class="info-item total">
            <span class="label">订单金额</span>
            <span class="value amount">¥{{ formatMoney(order.totalAmount) }}</span>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="订单不存在" />

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="商品评价" width="500px" :close-on-click-modal="false">
      <div class="review-dialog-content" v-if="currentReviewItem">
        <div class="review-product-info">
          <img :src="normalizeImageUrl(currentReviewItem.productImage, defaultImage)" class="review-product-image" />
          <span>{{ currentReviewItem.productName }}</span>
        </div>
        <el-form :model="reviewForm" label-position="top">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" :colors="['#F56C6C', '#E6A23C', '#67C23A']" show-text :texts="['很差', '差', '一般', '好', '很好']" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="分享您的使用感受..." maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="上传图片（可选）">
            <el-input v-model="reviewForm.images" placeholder="图片URL，多个用逗号分隔" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewSubmitting">提交评价</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, confirmReceive, cancelOrder } from '@/api/order'
import { addReview, checkReviewed } from '@/api/review'
import { OrderStatusText } from '@/types'
import type { Order, ReviewDTO } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { defaultImage, normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref<Order | null>(null)

// 评价相关
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const currentReviewItem = ref<any>(null)
const reviewForm = ref({
  rating: 5,
  content: '',
  images: ''
})

function getStatusText(status: number) {
  return OrderStatusText[status] || '未知'
}

function getStatusColor(status: number) {
  const colors: Record<number, string> = {
    0: '#E6A23C', // 待付款
    1: '#409EFF', // 待发货
    2: '#409EFF', // 待收货 (原 DELIVERED 是 2 已发货，现在 2 就是待收货)
    3: '#67C23A', // 已完成
    4: '#909399', // 已取消
    5: '#E6A23C', // 退款中
    6: '#909399'  // 已退款
  }
  return colors[status] || '#909399'
}

function getStatusIcon(status: number) {
  const icons: Record<number, string> = {
    0: 'Timer',       // 待付款
    1: 'Box',         // 待发货
    2: 'Van',         // 待收货
    3: 'CircleCheck', // 已完成
    4: 'CircleClose', // 已取消
    5: 'RefreshRight',// 退款中
    6: 'Finished'     // 已退款
  }
  return icons[status] || 'Document'
}

function getStatusDesc(status: number) {
  const descs: Record<number, string> = {
    0: '请在30分钟内完成支付',
    1: '商家正在准备发货',
    2: '商品配送中',
    3: '感谢您的购买',
    4: '订单已取消',
    5: '退款申请处理中',
    6: '退款已完成'
  }
  return descs[status] || ''
}

function toNumber(value: unknown) {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function formatMoney(value: unknown) {
  return toNumber(value).toFixed(2)
}

function getItemSubtotal(item: any) {
  return item.totalPrice ?? item.totalAmount ?? toNumber(item.price) * toNumber(item.quantity)
}

async function loadOrder() {
  const orderNo = route.params.id as string
  if (!orderNo) return
  
  loading.value = true
  try {
    const res = await getOrderDetail(orderNo)
    order.value = res.data
    // 检查评价状态
    await checkReviewStatus()
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (!order.value) return
  try {
    await payOrder(order.value.orderNo)
    ElMessage.success('支付成功')
    loadOrder()
  } catch {}
}

async function handleReceive() {
  if (!order.value) return
  await ElMessageBox.confirm('确认已收到商品？', '确认收货')
  try {
    await confirmReceive(order.value.orderNo)
    ElMessage.success('确认收货成功')
    loadOrder()
  } catch {}
}

async function handleCancel() {
  if (!order.value) return
  await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', { type: 'warning' })
  try {
    await cancelOrder(order.value.orderNo)
    ElMessage.success('订单已取消')
    loadOrder()
  } catch {}
}

// 检查已评价状态
async function checkReviewStatus() {
  if (!order.value || order.value.status !== 3) return
  for (const item of order.value.orderItems || []) {
    try {
      const res = await checkReviewed(order.value.orderNo, item.productId)
      item.reviewed = res.data || false
    } catch {
      item.reviewed = false
    }
  }
}

// 打开评价弹窗
function openReviewDialog(item: any) {
  currentReviewItem.value = item
  reviewForm.value = { rating: 5, content: '', images: '' }
  reviewDialogVisible.value = true
}

// 提交评价
async function submitReview() {
  if (!order.value || !currentReviewItem.value) return
  if (reviewForm.value.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  
  reviewSubmitting.value = true
  try {
    const dto: ReviewDTO = {
      orderNo: order.value.orderNo,
      productId: currentReviewItem.value.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      images: reviewForm.value.images
    }
    await addReview(dto)
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    currentReviewItem.value.reviewed = true
  } catch (error: any) {
    ElMessage.error(error.message || '评价失败')
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-icon {
  font-size: 48px;
}

.status-info h2 {
  font-size: 24px;
  margin-bottom: 4px;
}

.status-desc {
  color: var(--color-text-secondary);
}

.section {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section h3 .el-icon {
  color: var(--color-primary);
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
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

.product-info .price {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.subtotal {
  font-weight: 600;
  color: var(--color-secondary);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  width: 100px;
  color: var(--color-text-secondary);
}

.info-item.total {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}

.info-item .amount {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-secondary);
}

/* 评价相关 */
.product-item .el-button {
  flex-shrink: 0;
}

.review-dialog-content {
  padding: 0 10px;
}

.review-product-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.review-product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.review-product-info span {
  font-weight: 500;
  color: var(--color-text-primary);
}
</style>
