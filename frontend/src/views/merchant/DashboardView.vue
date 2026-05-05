<template>
  <div class="merchant-dashboard">
    <h2>商家工作台</h2>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e8f5e9;">
          <el-icon color="#228B22"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">今日销售额</p>
          <p class="stat-value">¥{{ stats.todaySales.toFixed(2) }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff3e0;">
          <el-icon color="#FF8C00"><ShoppingCart /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">今日订单</p>
          <p class="stat-value">{{ stats.todayOrders }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #e3f2fd;">
          <el-icon color="#1976D2"><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">在售商品</p>
          <p class="stat-value">{{ stats.onSaleProducts }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #fce4ec;">
          <el-icon color="#E91E63"><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">待发货</p>
          <p class="stat-value">{{ stats.pendingShipment }}</p>
        </div>
      </div>
    </div>

    <!-- 数据统计面板 -->
    <div class="panel chart-panel">
      <div class="panel-header">
        <h3>数据统计</h3>
        <el-radio-group v-model="currentPeriod" size="small" @change="loadPeriodStats">
          <el-radio-button label="today">今日</el-radio-button>
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="quarter">本季度</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 时段汇总 -->
      <div class="period-summary">
        <div class="summary-item">
          <span class="summary-label">销售额</span>
          <span class="summary-value amount">¥{{ periodStats.periodSales.toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">订单数</span>
          <span class="summary-value">{{ periodStats.periodOrders }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">环比增长</span>
          <span class="summary-value" :class="periodStats.growthRate >= 0 ? 'up' : 'down'">
            {{ periodStats.growthRate >= 0 ? '↑' : '↓' }} {{ Math.abs(periodStats.growthRate) }}%
          </span>
        </div>
      </div>

      <!-- 销售额趋势 -->
      <div class="chart-section">
        <h4>销售额趋势</h4>
        <BaseChart v-if="periodStats.trendLabels.length > 0" :options="salesChartOptions" height="300px" />
        <div v-else class="chart-empty">暂无数据</div>
      </div>

      <!-- 订单量趋势 -->
      <div class="chart-section">
        <h4>订单量趋势</h4>
        <BaseChart v-if="periodStats.trendLabels.length > 0" :options="ordersChartOptions" height="300px" />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
    </div>
    
    <div class="dashboard-grid">
      <!-- 最近订单 -->
      <div class="panel">
        <div class="panel-header">
          <h3>最近订单</h3>
          <el-button text type="primary" @click="$router.push('/merchant/orders')">查看全部</el-button>
        </div>
        <el-table class="admin-data-table" :data="recentOrders" :fit="false" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="210" class-name="admin-ellipsis" show-overflow-tooltip />
          <el-table-column prop="totalAmount" label="金额" width="86">
            <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="86">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="168" class-name="admin-nowrap">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 热销商品 -->
      <div class="panel">
        <div class="panel-header">
          <h3>热销商品</h3>
          <el-button text type="primary" @click="$router.push('/merchant/products')">查看全部</el-button>
        </div>
        <div class="product-rank-list">
          <div v-for="(item, index) in hotProducts" :key="item.id" class="product-rank-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <img :src="item.mainImage" :alt="item.name" class="product-img" @error="(e: Event) => (e.target as HTMLImageElement).src = defaultImage" />
            <div class="product-info">
              <p class="name">{{ item.name }}</p>
              <p class="sales">销量: {{ item.sales || 0 }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { OrderStatusText } from '@/types'
import type { Order, Product } from '@/types'
import { getMerchantProductList, getMerchantOrderList, getMerchantOverview, getMerchantPeriodStats } from '@/api/merchant'
import BaseChart from '@/components/BaseChart.vue'
import type { EChartsOption } from 'echarts'
import { formatDateTime } from '@/utils/format'

const defaultImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Crect fill='%23f5f5f5' width='200' height='200'/%3E%3Ctext x='100' y='110' text-anchor='middle' font-size='48'%3E🍊%3C/text%3E%3C/svg%3E"

const stats = reactive({
  todaySales: 0,
  todayOrders: 0,
  onSaleProducts: 0,
  pendingShipment: 0
})

const recentOrders = ref<Order[]>([])
const hotProducts = ref<Product[]>([])

const currentPeriod = ref('week')
const periodStats = reactive({
  periodSales: 0,
  periodOrders: 0,
  growthRate: 0,
  trendLabels: [] as string[],
  trendSales: [] as number[],
  trendOrders: [] as number[]
})

// 销售额趋势图配置
const salesChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis', formatter: '{b}<br/>销售额: ¥{c}' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: periodStats.trendLabels, axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
  series: [{
    type: 'bar',
    data: periodStats.trendSales,
    itemStyle: { color: '#10b981', borderRadius: [4, 4, 0, 0] },
    barMaxWidth: 40
  }]
}))

// 订单量趋势图配置
const ordersChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: periodStats.trendLabels, axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    data: periodStats.trendOrders,
    smooth: true,
    areaStyle: { color: 'rgba(245, 158, 11, 0.15)' },
    lineStyle: { color: '#f59e0b', width: 2 },
    itemStyle: { color: '#f59e0b' }
  }]
}))

function getStatusText(status: number) {
  return OrderStatusText[status] || '未知'
}

function getStatusType(status: number) {
  const types: Record<number, string> = {
    0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info'
  }
  return types[status] || 'info'
}

async function loadPeriodStats() {
  try {
    const res = await getMerchantPeriodStats(currentPeriod.value)
    const data = res.data as any
    if (data) {
      periodStats.periodSales = data.periodSales || 0
      periodStats.periodOrders = data.periodOrders || 0
      periodStats.growthRate = data.growthRate || 0
      periodStats.trendLabels = data.trendLabels || []
      periodStats.trendSales = (data.trendSales || []).map((v: any) => Number(v) || 0)
      periodStats.trendOrders = (data.trendOrders || []).map((v: any) => Number(v) || 0)
    }
  } catch (error) {
    console.error('加载时段统计失败:', error)
  }
}

async function loadDashboard() {
  try {
    // 并行加载统计数据、最近订单和热销商品
    const [overviewRes, orderRes, productRes] = await Promise.all([
      getMerchantOverview(),
      getMerchantOrderList({ pageNum: 1, pageSize: 5 }),
      getMerchantProductList({ status: 1, pageNum: 1, pageSize: 10 })
    ])

    // 统计概览
    const overview = overviewRes.data as any
    if (overview) {
      stats.todaySales = overview.todaySales || 0
      stats.todayOrders = overview.todayOrders || 0
      stats.onSaleProducts = overview.onSaleProducts || 0
      stats.pendingShipment = overview.pendingShipment || 0
    }

    // 最近订单
    recentOrders.value = ((orderRes.data as any)?.records || (orderRes.data as any)?.list || []).slice(0, 3)

    // 热销商品（按销量排序，取前3）
    const products = (productRes.data as any)?.records || (productRes.data as any)?.list || []
    hotProducts.value = products
      .sort((a: Product, b: Product) => (b.sales || 0) - (a.sales || 0))
      .slice(0, 3)
  } catch (error) {
    console.error('加载商家工作台数据失败:', error)
  }
}

onMounted(() => {
  loadDashboard()
  loadPeriodStats()
})
</script>

<style scoped>
.merchant-dashboard h2 {
  margin-bottom: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon .el-icon {
  font-size: 28px;
}

.stat-label {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

/* 数据统计面板 */
.chart-panel {
  margin-bottom: 24px;
}

.period-summary {
  display: flex;
  gap: 48px;
  padding: 16px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.summary-value.amount {
  color: var(--color-secondary);
}

.summary-value.up {
  color: #67C23A;
}

.summary-value.down {
  color: #F56C6C;
}

.chart-section {
  margin-bottom: 24px;
}

.chart-section:last-child {
  margin-bottom: 0;
}

.chart-section h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

.chart-empty {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.panel {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-header h3 {
  font-size: 16px;
}

.product-rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.rank.top {
  background: var(--color-secondary);
  color: white;
}

.product-img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.product-info {
  flex: 1;
}

.product-info .name {
  font-weight: 500;
  margin-bottom: 2px;
}

.product-info .sales {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
