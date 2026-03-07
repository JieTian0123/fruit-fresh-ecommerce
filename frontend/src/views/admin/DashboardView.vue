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
        <div class="bar-chart" v-if="periodStats.trendLabels.length > 0">
          <div class="chart-bars">
            <div
              v-for="(val, idx) in periodStats.trendSales"
              :key="'s' + idx"
              class="bar-item"
            >
              <div class="bar-wrapper">
                <div
                  class="bar sales-bar"
                  :style="{ height: getBarHeight(val, maxSales) + '%' }"
                >
                  <span class="bar-tooltip" v-if="val > 0">¥{{ val.toFixed(0) }}</span>
                </div>
              </div>
              <span class="bar-label">{{ periodStats.trendLabels[idx] }}</span>
            </div>
          </div>
        </div>
        <div v-else class="chart-empty">暂无数据</div>
      </div>

      <!-- 订单量趋势 -->
      <div class="chart-section">
        <h4>订单量趋势</h4>
        <div class="bar-chart" v-if="periodStats.trendLabels.length > 0">
          <div class="chart-bars">
            <div
              v-for="(val, idx) in periodStats.trendOrders"
              :key="'o' + idx"
              class="bar-item"
            >
              <div class="bar-wrapper">
                <div
                  class="bar order-bar"
                  :style="{ height: getBarHeight(val, maxOrders) + '%' }"
                >
                  <span class="bar-tooltip" v-if="val > 0">{{ val }}</span>
                </div>
              </div>
              <span class="bar-label">{{ periodStats.trendLabels[idx] }}</span>
            </div>
          </div>
        </div>
        <div v-else class="chart-empty">暂无数据</div>
      </div>
    </div>
    
    <div class="dashboard-grid">
      <!-- 最新订单 -->
      <div class="panel">
        <div class="panel-header">
          <h3>最新订单</h3>
          <el-button text type="primary" @click="$router.push('/admin/orders')">查看全部</el-button>
        </div>
        <el-table :data="recentOrders" size="small">
          <el-table-column prop="orderNo" label="订单号" width="160" />
          <el-table-column prop="totalAmount" label="金额">
            <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="140" />
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
              <p class="time">{{ user.createTime }}</p>
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
import { getUserList, getOrderListForAdmin, getAdminOverview, getAdminGrowth, getAdminPeriodStats } from '@/api/admin'

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

const maxSales = computed(() => {
  if (periodStats.trendSales.length === 0) return 1
  const max = Math.max(...periodStats.trendSales)
  return max > 0 ? max : 1
})

const maxOrders = computed(() => {
  if (periodStats.trendOrders.length === 0) return 1
  const max = Math.max(...periodStats.trendOrders)
  return max > 0 ? max : 1
})

function getBarHeight(value: number, max: number): number {
  if (max === 0) return 0
  return Math.max((value / max) * 100, value > 0 ? 4 : 0)
}

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
  } catch (error) {
    console.error('加载时段统计失败:', error)
  }
}

async function loadDashboard() {
  try {
    // 并行加载统计数据、增长率、最新订单和最新用户
    const [overviewRes, growthRes, orderRes, recentUserRes] = await Promise.all([
      getAdminOverview(),
      getAdminGrowth(),
      getOrderListForAdmin({ pageNum: 1, pageSize: 5 }),
      getUserList({ userType: 0, pageNum: 1, pageSize: 3 })
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

.bar-chart {
  overflow-x: auto;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 160px;
  min-width: 100%;
  padding: 0 4px;
}

.bar-item {
  flex: 1;
  min-width: 20px;
  max-width: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 70%;
  min-height: 2px;
  border-radius: 3px 3px 0 0;
  transition: height 0.3s ease;
  position: relative;
  cursor: pointer;
}

.bar:hover .bar-tooltip {
  opacity: 1;
}

.bar-tooltip {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  white-space: nowrap;
  color: var(--color-text-primary);
  background: white;
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.sales-bar {
  background: linear-gradient(180deg, #10b981, #059669);
}

.order-bar {
  background: linear-gradient(180deg, #f59e0b, #d97706);
}

.bar-label {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  text-align: center;
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
