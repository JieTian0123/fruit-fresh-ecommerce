<template>
  <div class="order-list-page">
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
    </div>
    
    <!-- 订单状态筛选 -->
    <div class="order-tabs">
      <el-tabs v-model="currentStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="待评价" name="review" />
      </el-tabs>
    </div>
    
    <!-- 待评价列表 -->
    <div class="review-list" v-if="currentStatus === 'review'" v-loading="loading">
      <template v-if="pendingItems.length > 0">
        <div v-for="item in pendingItems" :key="`${item.orderNo}-${item.productId}`" class="review-card">
          <div class="review-header">
            <span class="order-no">订单号：{{ item.orderNo }}</span>
            <span class="order-time">{{ formatDateTime(item.createTime) }}</span>
          </div>
          <div class="review-content">
            <img :src="normalizeImageUrl(item.productImage, defaultImage)" :alt="item.productName" class="product-image" />
            <div class="product-info">
              <h4>{{ item.productName }}</h4>
              <p class="price">¥{{ formatMoney(item.price) }} × {{ item.quantity }}</p>
            </div>
            <el-button
              :type="item.reviewed ? 'info' : 'primary'"
              :disabled="item.reviewed"
              size="default"
              @click="openReviewDialog(item)"
            >
              {{ item.reviewed ? '已评价' : '写评价' }}
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无待评价商品" />
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-else v-loading="loading">
      <template v-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
            <el-tag :type="getStatusType(order.status)" size="small">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
          
          <div class="order-content" @click="goToDetail(order.orderNo)">
            <!-- 订单商品摘要 -->
            <div v-if="order.orderItems && order.orderItems.length > 0">
              <div v-for="item in order.orderItems.slice(0, 3)" :key="item.id" class="order-item">
                <img :src="normalizeImageUrl(item.productImage, defaultImage)" :alt="item.productName" class="item-image" />
                <div class="item-info">
                  <h4>{{ item.productName }}</h4>
                  <p>¥{{ formatMoney(item.price) }} × {{ item.quantity }}</p>
                </div>
              </div>
              <div v-if="order.orderItems.length > 3" class="more-items">
                还有 {{ order.orderItems.length - 3 }} 件商品...
              </div>
            </div>
            
            <!-- 仅当无商品信息时显示收货人 -->
            <div v-else class="order-summary">
              <p>收货人：{{ order.receiverName }} {{ order.receiverPhone }}</p>
              <p class="address">{{ order.receiverAddress }}</p>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              合计：<span class="amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <el-button v-if="order.status === 0" type="danger" @click.stop="handleCancel(order.orderNo)">
                取消订单
              </el-button>
              <el-button v-if="order.status === 0" type="primary" @click.stop="handlePay(order.orderNo)">
                立即付款
              </el-button>
              <el-button v-if="order.status === 2" type="primary" @click.stop="handleReceive(order.orderNo)">
                确认收货
              </el-button>
              <el-button @click="goToDetail(order.orderNo)">查看详情</el-button>
            </div>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="暂无订单" />
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="商品评价" width="500px" :close-on-click-modal="false">
      <div class="review-dialog-content" v-if="currentReviewItem">
        <div class="review-product-info">
        <img :src="normalizeImageUrl(currentReviewItem.productImage, defaultImage)" class="review-product-image" />
          <span>{{ currentReviewItem.productName }}</span>
        </div>
        <el-form :model="reviewForm" label-position="top">
          <el-form-item label="评分">
            <el-rate
              v-model="reviewForm.rating"
              :colors="['#F56C6C', '#E6A23C', '#67C23A']"
              show-text
              :texts="['很差', '差', '一般', '好', '很好']"
            />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="分享您的使用感受..."
              maxlength="500"
              show-word-limit
            />
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
import { useRouter, useRoute } from 'vue-router'
import { getOrderList, payOrder, confirmReceive, cancelOrder } from '@/api/order'
import { addReview, checkReviewed } from '@/api/review'
import { OrderStatusText, OrderStatusColor } from '@/types'
import type { Order, ReviewDTO } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { defaultImage, normalizeImageUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref<Order[]>([])
const currentStatus = ref('all')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 待评价相关
interface PendingReviewItem {
  orderNo: string
  createTime: string
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  reviewed: boolean
}

const pendingItems = ref<PendingReviewItem[]>([])
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const currentReviewItem = ref<PendingReviewItem | null>(null)
const reviewForm = ref({ rating: 5, content: '', images: '' })

function getStatusText(status: number) {
  return OrderStatusText[status] || '未知'
}

function getStatusType(status: number) {
  return OrderStatusColor[status] || 'info'
}

function formatMoney(value: unknown) {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

function handleTabChange() {
  pageNum.value = 1
  if (currentStatus.value === 'review') {
    loadPendingReviews()
  } else {
    loadOrders()
  }
}

function handlePageChange() {
  if (currentStatus.value === 'review') {
    loadPendingReviews()
  } else {
    loadOrders()
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (currentStatus.value !== 'all') {
      params.status = Number(currentStatus.value)
    }
    const res = await getOrderList(params)
    // 兼容 list 和 records
    orders.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function goToDetail(orderNo: string) {
  router.push(`/order/${orderNo}`)
}

async function handlePay(orderNo: string) {
  try {
    await payOrder(orderNo)
    ElMessage.success('支付成功')
    loadOrders()
  } catch {
    // 错误处理
  }
}

async function handleReceive(orderNo: string) {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货')
  try {
    await confirmReceive(orderNo)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch {
    // 错误处理
  }
}

async function handleCancel(orderNo: string) {
  await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', { type: 'warning' })
  try {
    await cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {
    // 错误处理
  }
}

async function loadPendingReviews() {
  loading.value = true
  try {
    const res = await getOrderList({ status: 3, pageNum: pageNum.value, pageSize: pageSize.value })
    const completedOrders: Order[] = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0

    const items: PendingReviewItem[] = []
    for (const order of completedOrders) {
      const orderItems = order.orderItems || (order as any).items || []
      for (const item of orderItems) {
        items.push({
          orderNo: order.orderNo,
          createTime: order.createTime,
          productId: item.productId,
          productName: item.productName,
          productImage: item.productImage,
          price: item.price,
          quantity: item.quantity,
          reviewed: false
        })
      }
    }

    await Promise.all(
      items.map(async (item) => {
        try {
          const reviewRes = await checkReviewed(item.orderNo, item.productId)
          item.reviewed = reviewRes.data || false
        } catch {
          item.reviewed = false
        }
      })
    )

    items.sort((a, b) => {
      if (a.reviewed === b.reviewed) return 0
      return a.reviewed ? 1 : -1
    })

    pendingItems.value = items
  } catch {
    pendingItems.value = []
  } finally {
    loading.value = false
  }
}

function openReviewDialog(item: PendingReviewItem) {
  currentReviewItem.value = item
  reviewForm.value = { rating: 5, content: '', images: '' }
  reviewDialogVisible.value = true
}

async function submitReview() {
  if (!currentReviewItem.value) return
  if (reviewForm.value.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }

  reviewSubmitting.value = true
  try {
    const dto: ReviewDTO = {
      orderNo: currentReviewItem.value.orderNo,
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
  const statusQuery = route.query.status
  if (statusQuery && ['0', '1', '2', '3', 'review'].includes(statusQuery as string)) {
    currentStatus.value = statusQuery as string
  }
  if (currentStatus.value === 'review') {
    loadPendingReviews()
  } else {
    loadOrders()
  }
})
</script>

<style scoped>
.order-list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-title {
  font-size: 24px;
}

.order-tabs {
  background: white;
  border-radius: var(--radius-lg);
  padding: 0 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #f8f9fa;
  font-size: 14px;
}

.order-no {
  font-weight: 500;
}

.order-time {
  color: var(--color-text-secondary);
  margin-left: auto;
}

.order-content {
  padding: 20px;
  cursor: pointer;
}

.order-content:hover {
  background: #fafafa;
}

.order-summary p {
  margin: 4px 0;
  font-size: 14px;
}

.order-summary .address {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.item-info h4 {
  font-size: 14px;
  margin-bottom: 4px;
}

.item-info p {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.more-items {
  color: var(--color-text-light);
  font-size: 13px;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.order-total .amount {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-secondary);
}

.order-actions {
  display: flex;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
}

/* 待评价列表 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #f8f9fa;
  font-size: 14px;
}

.review-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
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
  font-size: 14px;
  margin-bottom: 4px;
}

.product-info .price {
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* 评价弹窗 */
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
