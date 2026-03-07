<template>
  <div class="coupon-view">
    <div class="coupon-header">
      <h2>优惠券中心</h2>
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="可领取" name="available"></el-tab-pane>
        <el-tab-pane label="我的优惠券" name="my"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 可领取优惠券 -->
    <div v-if="activeTab === 'available'" class="coupon-list">
      <el-empty v-if="availableCoupons.length === 0" description="暂无可领取的优惠券" />
      <div v-else class="coupon-grid">
        <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-left">
            <div class="coupon-amount">
              <template v-if="coupon.couponType === 2">
                <span class="value fold">{{ (coupon.discountRate * 10).toFixed(1) }}折</span>
              </template>
              <template v-else>
                <span class="symbol">¥</span>
                <span class="value">{{ coupon.discountAmount }}</span>
              </template>
            </div>
            <div class="coupon-condition">
              {{ coupon.minimumAmount ? `满${coupon.minimumAmount}元可用` : '无门槛' }}
            </div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.title }}</div>
            <div class="coupon-desc" v-if="coupon.description">{{ coupon.description }}</div>
            <div class="coupon-time">
              有效期：{{ formatDate(coupon.validFrom) }} 至 {{ formatDate(coupon.validUntil) }}
            </div>
            <div class="coupon-count">
              剩余：{{ (coupon.totalQuantity ?? 0) - (coupon.receivedQuantity ?? 0) }}/{{ coupon.totalQuantity ?? 0 }}
            </div>
            <el-button
              type="primary"
              size="small"
              :disabled="(coupon.totalQuantity ?? 0) - (coupon.receivedQuantity ?? 0) <= 0"
              @click="handleReceive(coupon.id)"
            >
              {{ (coupon.totalQuantity ?? 0) - (coupon.receivedQuantity ?? 0) <= 0 ? '已抢完' : '立即领取' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的优惠券 -->
    <div v-else class="my-coupons">
      <el-tabs v-model="myCouponTab" @tab-click="loadMyCoupons">
        <el-tab-pane label="未使用" name="unused"></el-tab-pane>
        <el-tab-pane label="已使用" name="used"></el-tab-pane>
        <el-tab-pane label="已过期" name="expired"></el-tab-pane>
      </el-tabs>

      <el-empty v-if="myCoupons.length === 0" description="暂无优惠券" />
      <div v-else class="coupon-grid">
        <div v-for="item in myCoupons" :key="item.id" class="coupon-card" :class="{'disabled': item.status !== 0}">
          <div class="coupon-left">
            <div class="coupon-amount">
              <template v-if="item.couponType === 2">
                <span class="value fold">{{ (item.discountRate * 10).toFixed(1) }}折</span>
              </template>
              <template v-else>
                <span class="symbol">¥</span>
                <span class="value">{{ item.discountAmount }}</span>
              </template>
            </div>
            <div class="coupon-condition">
              {{ item.minimumAmount ? `满${item.minimumAmount}元可用` : '无门槛' }}
            </div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ item.title }}</div>
            <div class="coupon-desc" v-if="item.description">{{ item.description }}</div>
            <div class="coupon-time">
              有效期：{{ formatDate(item.validFrom) }} 至 {{ formatDate(item.validUntil) }}
            </div>
            <div class="coupon-status">
              <el-tag v-if="item.status === 0" type="success">未使用</el-tag>
              <el-tag v-else-if="item.status === 1" type="info">已使用</el-tag>
              <el-tag v-else type="danger">已过期</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, getMyCoupons, receiveCoupon } from '@/api/coupon'

const activeTab = ref('available')
const myCouponTab = ref('unused')
const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])

// 加载可领取优惠券
const loadAvailableCoupons = async () => {
  try {
    const res = await getAvailableCoupons()
    if (res.code === 200) {
      // API 返回分页结构 res.data.records，兼容直接数组
      availableCoupons.value = (res.data as any)?.records ?? (res.data as any) ?? []
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

// 加载我的优惠券
const loadMyCoupons = async () => {
  try {
    const statusMap: Record<string, string> = {
      'unused': '0',
      'used': '1',
      'expired': '2'
    }
    const res = await getMyCoupons(statusMap[myCouponTab.value])
    if (res.code === 200) {
      // 我的优惠券接口返回 UserCoupon 列表，字段直接挂在条目上
      const records = (res.data as any)?.records ?? (res.data as any) ?? []
      // 若接口返回的是 UserCoupon（含 coupon 嵌套），展平字段；否则直接使用
      myCoupons.value = records.map((item: any) => {
        if (item.coupon) {
          // 嵌套结构：将 coupon 字段提升到顶层，保留 item.status/validFrom/validUntil
          return {
            ...item.coupon,
            id: item.id,
            status: item.status,
            validFrom: item.validFrom ?? item.coupon.validFrom,
            validUntil: item.validUntil ?? item.coupon.validUntil,
          }
        }
        return item
      })
    }
  } catch (error) {
    console.error('加载我的优惠券失败:', error)
  }
}

// 切换Tab
const handleTabChange = () => {
  if (activeTab.value === 'available') {
    loadAvailableCoupons()
  } else {
    loadMyCoupons()
  }
}

// 领取优惠券
const handleReceive = async (couponId: number) => {
  try {
    const res = await receiveCoupon(couponId)
    if (res.code === 200) {
      ElMessage.success('领取成功')
      loadAvailableCoupons()
    } else {
      ElMessage.error(res.message || '领取失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '领取失败')
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadAvailableCoupons()
})
</script>

<style scoped>
.coupon-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .coupon-header {
    margin-bottom: 30px;
    
    h2 {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 20px;
    }
  }

  .coupon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
    gap: 20px;
  }

  .coupon-card {
    display: flex;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.disabled {
      opacity: 0.6;
      background: linear-gradient(135deg, #bdc3c7 0%, #95a5a6 100%);
    }

    .coupon-left {
      width: 140px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      padding: 20px;

      .coupon-amount {
        display: flex;
        align-items: baseline;
        margin-bottom: 10px;

        .symbol {
          font-size: 20px;
        }

        .value {
          font-size: 48px;
          font-weight: bold;
          line-height: 1;

          &.fold {
            font-size: 32px;
          }
        }
      }

      .coupon-condition {
        font-size: 14px;
        opacity: 0.9;
        text-align: center;
      }
    }

    .coupon-right {
      flex: 1;
      padding: 20px;
      background: white;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .coupon-name {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin-bottom: 8px;
      }

      .coupon-desc {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
      }

      .coupon-time {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }

      .coupon-count {
        font-size: 12px;
        color: #999;
        margin-bottom: 12px;
      }

      .coupon-status {
        margin-bottom: 12px;
      }

      .el-button {
        align-self: flex-start;
      }
    }
  }
}
</style>
