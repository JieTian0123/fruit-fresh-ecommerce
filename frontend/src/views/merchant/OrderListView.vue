<template>
  <div class="order-list-page">
    <div class="header">
      <h2>订单管理</h2>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 180px" />
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 120px">
        <el-option label="待付款" :value="0" />
        <el-option label="待发货" :value="1" />
        <el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 260px"
      />
      <el-button type="primary" @click="loadOrders">搜索</el-button>
    </div>
    
    <!-- 订单表格 -->
    <el-table :data="orderList" v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="商品" min-width="250">
        <template #default="{ row }">
          <div class="product-preview">
            <img v-if="row.items?.[0]" :src="row.items[0].productImage" class="product-img" />
            <span>{{ row.items?.[0]?.productName || '-' }}</span>
            <span v-if="row.items?.length > 1" class="more">等{{ row.items.length }}件</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 1" text type="success" @click="handleShip(row)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadOrders"
      />
    </div>
    
    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="发货" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.deliveryCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="shipForm.deliveryNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>
    
    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="订单详情" size="500px">
      <template v-if="currentOrder">
        <div class="order-detail">
          <div class="section">
            <h4>基本信息</h4>
            <p><span>订单号：</span>{{ currentOrder.orderNo }}</p>
            <p><span>状态：</span><el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag></p>
            <p><span>下单时间：</span>{{ currentOrder.createTime }}</p>
            <p v-if="currentOrder.payTime"><span>支付时间：</span>{{ currentOrder.payTime }}</p>
          </div>
          
          <div class="section" v-if="currentOrder.address">
            <h4>收货信息</h4>
            <p><span>收货人：</span>{{ currentOrder.address.receiverName }} {{ currentOrder.address.receiverPhone }}</p>
            <p><span>地址：</span>{{ currentOrder.address.province }} {{ currentOrder.address.city }} {{ currentOrder.address.district }} {{ currentOrder.address.detailAddress }}</p>
          </div>
          
          <div class="section">
            <h4>商品信息</h4>
            <div v-for="item in currentOrder.items" :key="item.id" class="item-row">
              <img :src="item.productImage" class="item-img" />
              <div class="item-info">
                <p>{{ item.productName }}</p>
                <p class="price">¥{{ item.price?.toFixed(2) }} × {{ item.quantity }}</p>
              </div>
              <div class="subtotal">¥{{ item.totalPrice?.toFixed(2) }}</div>
            </div>
          </div>
          
          <div class="section total-section">
            <p><span>订单金额：</span><strong>¥{{ currentOrder.totalAmount?.toFixed(2) }}</strong></p>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMerchantOrderList } from '@/api/merchant'
import { shipOrder } from '@/api/order'
import { OrderStatusText } from '@/types'
import type { Order } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const shipping = ref(false)
const orderList = ref<Order[]>([])
const total = ref(0)
const dateRange = ref<[Date, Date] | null>(null)
const shipDialogVisible = ref(false)
const detailDrawerVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const query = reactive({
  orderNo: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

const shipForm = reactive({
  orderNo: '',
  deliveryCompany: '',
  deliveryNo: ''
})

function getStatusText(status: number) {
  return OrderStatusText[status] || '未知'
}

function getStatusType(status: number) {
  const types: Record<number, string> = {
    0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info'
  }
  return types[status] || 'info'
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await getMerchantOrderList({
      orderNo: query.orderNo,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    orderList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    orderList.value = []
  } finally {
    loading.value = false
  }
}

function handleView(order: Order) {
  currentOrder.value = order
  detailDrawerVisible.value = true
}

function handleShip(order: Order) {
  shipForm.orderNo = order.orderNo
  shipForm.deliveryCompany = ''
  shipForm.deliveryNo = ''
  shipDialogVisible.value = true
}

async function confirmShip() {
  if (!shipForm.deliveryCompany || !shipForm.deliveryNo) {
    ElMessage.warning('请填写完整的物流信息')
    return
  }
  
  shipping.value = true
  try {
    await shipOrder({
      orderNo: shipForm.orderNo,
      deliveryCompany: shipForm.deliveryCompany,
      deliveryNo: shipForm.deliveryNo
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadOrders()
  } finally {
    shipping.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list-page {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 18px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.product-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.more {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.order-detail .section {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-detail .section h4 {
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

.order-detail .section p {
  margin-bottom: 8px;
  color: var(--color-text-secondary);
}

.order-detail .section p span {
  color: var(--color-text-primary);
}

.item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.item-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.item-info {
  flex: 1;
}

.item-info .price {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.subtotal {
  font-weight: 600;
  color: var(--color-secondary);
}

.total-section strong {
  font-size: 18px;
  color: var(--color-secondary);
}
</style>
