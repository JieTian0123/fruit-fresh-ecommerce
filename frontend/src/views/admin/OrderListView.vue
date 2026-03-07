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
        <el-option label="申请退款" :value="5" />
        <el-option label="已退款" :value="6" />
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
      <el-button @click="handleExport">导出</el-button>
    </div>
    
    <!-- 订单表格 -->
    <el-table :data="orderList" v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-preview">
            <img v-if="row.items?.[0]" :src="row.items[0].productImage" class="product-img" />
            <span>{{ row.items?.[0]?.productName || '-' }}</span>
            <span v-if="row.items?.length > 1" class="more">等{{ row.items.length }}件</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="merchantName" label="商家" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleView(row)">详情</el-button>
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
    
    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="订单详情" size="600px">
      <template v-if="currentOrder">
        <div class="order-detail">
          <el-descriptions title="基本信息" :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="订单备注">{{ currentOrder.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
          
          <el-descriptions title="收货信息" :column="1" border style="margin-top: 24px" v-if="currentOrder.address">
            <el-descriptions-item label="收货人">{{ currentOrder.address.receiverName }} {{ currentOrder.address.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ currentOrder.address.province }} {{ currentOrder.address.city }} {{ currentOrder.address.district }} {{ currentOrder.address.detailAddress }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="section" style="margin-top: 24px">
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
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOrderListForAdmin } from '@/api/admin'
import { OrderStatusText } from '@/types'
import type { Order } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const orderList = ref<Order[]>([])
const total = ref(0)
const dateRange = ref<[Date, Date] | null>(null)
const detailDrawerVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const query = reactive({
  orderNo: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

function getStatusText(status: number) {
  return OrderStatusText[status] || '未知'
}

function getStatusType(status: number) {
  const types: Record<number, string> = {
    0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info', 5: 'warning', 6: 'info'
  }
  return types[status] || 'info'
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrderListForAdmin({
      orderNo: query.orderNo,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    orderList.value = res.data?.records || res.data?.list || []
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

function handleExport() {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中')
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

.section h4 {
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--color-text-primary);
}

.item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item-row:last-child {
  border-bottom: none;
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
  margin-top: 4px;
}

.subtotal {
  font-weight: 600;
  color: var(--color-secondary);
}
</style>
