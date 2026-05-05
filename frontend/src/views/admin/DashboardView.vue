<template>
  <div class="admin-dashboard">
    <h2>管理后台</h2>

    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e8f5e9;">
          <el-icon color="#228B22"><User /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">用户总数</p>
          <p class="stat-value">{{ stats.totalUsers }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fff3e0;">
          <el-icon color="#FF8C00"><Shop /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">商家总数</p>
          <p class="stat-value">{{ stats.totalMerchants }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #e3f2fd;">
          <el-icon color="#1976D2"><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">商品总数</p>
          <p class="stat-value">{{ stats.totalProducts }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fce4ec;">
          <el-icon color="#E91E63"><ShoppingCart /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">订单总数</p>
          <p class="stat-value">{{ stats.totalOrders }}</p>
        </div>
      </div>
    </div>

    <!-- 销售统计 -->
    <div class="stat-cards secondary">
      <div class="stat-card wide">
        <div class="stat-info">
          <p class="stat-label">今日销售额</p>
          <p class="stat-value amount">¥{{ stats.todaySales.toFixed(2) }}</p>
          <p class="stat-compare">
            <span :class="stats.salesGrowth >= 0 ? 'up' : 'down'">
              {{ stats.salesGrowth >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.salesGrowth) }}%
            </span>
            较昨日
          </p>
        </div>
      </div>

      <div class="stat-card wide">
        <div class="stat-info">
          <p class="stat-label">今日订单数</p>
          <p class="stat-value">{{ stats.todayOrders }}</p>
          <p class="stat-compare">
            <span :class="stats.ordersGrowth >= 0 ? 'up' : 'down'">
              {{ stats.ordersGrowth >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.ordersGrowth) }}%
            </span>
            较昨日
          </p>
        </div>
      </div>

      <div class="stat-card wide">
        <div class="stat-info">
          <p class="stat-label">今日新增用户</p>
          <p class="stat-value">{{ stats.todayNewUsers }}</p>
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

    <!-- 数据分析图表 -->
    <div class="charts-row">
      <div class="panel">
        <div class="panel-header">
          <h3>订单状态分布</h3>
        </div>
        <BaseChart :options="orderStatusChartOptions" height="300px" />
      </div>
      <div class="panel">
        <div class="panel-header">
          <h3>用户增长趋势</h3>
        </div>
        <BaseChart :options="userGrowthChartOptions" height="300px" />
      </div>
    </div>

    <div class="dashboard-grid">
      <!-- 最新订单 -->
      <div class="panel">
        <div class="panel-header">
          <h3>最新订单</h3>
          <el-button text type="primary" @click="$router.push('/admin/orders')">查看全部</el-button>
        </div>
        <el-table class="admin-data-table" :data="recentOrders" :fit="false" size="small">
          <el-table-column prop="orderNo" label="订单号" width="210" class-name="admin-ellipsis" show-overflow-tooltip />
          <el-table-column prop="totalAmount" label="金额" width="86">
            <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="86">
            <template #default="{ row }">
              <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="168" class-name="admin-nowrap">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 新注册用户 -->
      <div class="panel">
        <div class="panel-header">
          <h3>新注册用户</h3>
          <el-button text type="primary" @click="$router.push('/admin/users')">查看全部</el-button>
        </div>
        <div class="user-list">
          <div v-for="user in recentUsers" :key="user.id" class="user-item">
            <el-avatar :size="40">{{ user.nickname?.[0] || user.username?.[0] }}</el-avatar>
            <div class="user-info">
              <p class="name">{{ user.nickname || user.username }}</p>
              <p class="time">{{ formatDateTime(user.createTime) }}</p>
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
import type { Order, UserInfo } from '@/types'
import { getUserList, getOrderListForAdmin, getAdminOverview, getAdminGrowth, getAdminPeriodStats, getOrderStatusDistribution, getUserGrowthTrend } from '@/api/admin'
import BaseChart from '@/components/BaseChart.vue'
import type { EChartsOption } from 'echarts'
import { formatDateTime } from '@/utils/format'

const stats = reactive({
  totalUsers: 0,
  totalMerchants: 0,
  totalProducts: 0,
  totalOrders: 0,
  todaySales: 0,
  todayOrders: 0,
  todayNewUsers: 0,
  salesGrowth: 0,
  ordersGrowth: 0
})

const recentOrders = ref<Order[]>([])
const recentUsers = ref<UserInfo[]>([])

const currentPeriod = ref('week')
const periodStats = reactive({
  periodSales: 0,
  periodOrders: 0,
  growthRate: 0,
  trendLabels: [] as string[],
  trendSales: [] as number[],
  trendOrders: [] as number[]
})

const orderStatusData = ref([
  { value: 0, name: '待付款', itemStyle: { color: '#E6A23C' } },
  { value: 0, name: '待发货', itemStyle: { color: '#409EFF' } },
  { value: 0, name: '待收货', itemStyle: { color: '#67C23A' } },
  { value: 0, name: '已完成', itemStyle: { color: '#228B22' } },
  { value: 0, name: '已取消', itemStyle: { color: '#909399' } }
])

const userGrowthData = ref<number[]>([])

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

// 订单状态分布饼图
const orderStatusChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 12 } },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    center: ['50%', '45%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    data: orderStatusData.value
  }]
}))

// 用户增长趋势
const userGrowthChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: periodStats.trendLabels, axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    data: userGrowthData.value,
    smooth: true,
    areaStyle: { color: 'rgba(34, 139, 34, 0.12)' },
    lineStyle: { color: '#228B22', width: 2 },
    itemStyle: { color: '#228B22' }
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
    const res = await getAdminPeriodStats(currentPeriod.value)
    const data = res.data as any
    if (data) {
      periodStats.periodSales = data.periodSales || 0
      periodStats.periodOrders = data.periodOrders || 0
      periodStats.growthRate = data.growthRate || 0
      periodStats.trendLabels = data.trendLabels || []
      periodStats.trendSales = (data.trendSales || []).map((v: any) => Number(v) || 0)
      periodStats.trendOrders = (data.trendOrders || []).map((v: any) => Number(v) || 0)
    }

    // 加载用户增长趋势（使用当前时段）
    await loadUserGrowthTrend()
  } catch (error) {
    console.error('加载时段统计失败:', error)
  }
}

async function loadUserGrowthTrend() {
  try {
    // 根据当前时段选择API参数
    const period = currentPeriod.value === 'month' || currentPeriod.value === 'quarter' || currentPeriod.value === 'year' ? 'month' : 'week'
    const res = await getUserGrowthTrend(period)
    const data = res.data as any[]
    if (data && data.length > 0) {
      userGrowthData.value = data.map(item => item.count || 0)
    }
  } catch (error) {
    console.error('加载用户增长趋势失败:', error)
  }
}

async function loadDashboard() {
  try {
    // 并行加载统计数据、增长率、最新订单、最新用户、订单状态分布
    const [overviewRes, growthRes, orderRes, recentUserRes, orderStatusRes] = await Promise.all([
      getAdminOverview(),
      getAdminGrowth(),
      getOrderListForAdmin({ pageNum: 1, pageSize: 5 }),
      getUserList({ userType: 0, pageNum: 1, pageSize: 3 }),
      getOrderStatusDistribution()
    ])

    // 统计概览
    const overview = overviewRes.data as any
    if (overview) {
      stats.totalUsers = overview.totalUsers || 0
      stats.totalMerchants = overview.totalMerchants || 0
      stats.totalProducts = overview.totalProducts || 0
      stats.totalOrders = overview.totalOrders || 0
      stats.todaySales = overview.todaySales || 0
      stats.todayOrders = overview.todayOrders || 0
      stats.todayNewUsers = overview.todayNewUsers || 0
    }

    // 增长率
    const growth = growthRes.data as any
    if (growth) {
      stats.salesGrowth = growth.salesGrowth || 0
      stats.ordersGrowth = growth.ordersGrowth || 0
    }

    // 最新订单
    recentOrders.value = ((orderRes.data as any)?.records || (orderRes.data as any)?.list || []).slice(0, 4)

    // 最新用户
    recentUsers.value = (recentUserRes.data as any)?.records || (recentUserRes.data as any)?.list || []

    // 订单状态分布（使用真实API数据）
    const statusData = orderStatusRes.data as any[]
    if (statusData && statusData.length > 0) {
      orderStatusData.value = statusData
    } else {
      // 如果没有数据，显示空状态
      orderStatusData.value = [
        { value: 0, name: '待付款', itemStyle: { color: '#E6A23C' } },
        { value: 0, name: '待发货', itemStyle: { color: '#409EFF' } },
        { value: 0, name: '待收货', itemStyle: { color: '#67C23A' } },
        { value: 0, name: '已完成', itemStyle: { color: '#228B22' } },
        { value: 0, name: '已取消', itemStyle: { color: '#909399' } }
      ]
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

onMounted(() => {
  loadDashboard()
  loadPeriodStats()
})
</script>

<style scoped>
.admin-dashboard h2 {
  margin-bottom: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-cards.secondary {
  grid-template-columns: repeat(3, 1fr);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.stat-card.wide {
  flex-direction: column;
  align-items: flex-start;
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

.stat-value.amount {
  color: var(--color-secondary);
}

.stat-compare {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.stat-compare .up {
  color: #67C23A;
}

.stat-compare .down {
  color: #F56C6C;
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

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
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

.user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info .name {
  font-weight: 500;
}

.user-info .time {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
