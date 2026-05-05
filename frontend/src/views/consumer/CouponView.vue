<template>
  <div class="coupon-view">
    <div class="coupon-header">
      <h2>优惠券中心</h2>
      <el-tabs v-model="activeTab">
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
              <el-tag v-if="coupon.vipFreeReceive === 1" type="warning" size="small" style="margin-left: 8px;">VIP免费</el-tag>
            </div>
            <el-button
              type="primary"
              size="small"
              :disabled="getCouponButtonState(coupon).disabled"
              @click="handleReceive(coupon)"
            >
              {{ getCouponButtonState(coupon).text }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的优惠券 -->
    <div v-else class="my-coupons">
      <el-tabs v-model="myCouponTab">
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCoupons, getMyCoupons, receiveCoupon, exchangeCoupon } from '@/api/coupon'
import { getVipStatus } from '@/api/vip'
import { getPointsBalance } from '@/api/points'

const route = useRoute()
const activeTab = ref('available')
const myCouponTab = ref('unused')
const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])
const receivedCouponIds = ref<number[]>([])
const couponAcquireTypeMap = ref<Record<string, 'receive' | 'exchange'>>({})
const isVip = ref(false)
const userPoints = ref(0)

// 竞态防护：我的优惠券请求序号
let myCouponRequestId = 0

// 加载积分余额
const loadPointsBalance = async () => {
  try {
    const res = await getPointsBalance()
    if (res.code === 200) {
      userPoints.value = res.data ?? 0
    }
  } catch {
    userPoints.value = 0
  }
}

// 加载可领取优惠券
const loadAvailableCoupons = async () => {
  try {
    const res = await getAvailableCoupons()
    if (res.code === 200) {
      // API 返回分页结构 res.data.records，兼容直接数组
      availableCoupons.value = (res.data as any)?.records ?? (res.data as any) ?? []
      // 获取用户已领取的优惠券ID列表
      receivedCouponIds.value = (res.data as any)?.receivedCouponIds ?? []
      couponAcquireTypeMap.value = (res.data as any)?.acquireTypeMap ?? {}
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

// 加载我的优惠券（带竞态防护）
const loadMyCoupons = async () => {
  const reqId = ++myCouponRequestId
  try {
    const statusMap: Record<string, number> = {
      'unused': 0,
      'used': 1,
      'expired': 2
    }
    const res = await getMyCoupons(statusMap[myCouponTab.value])
    // 竞态防护：忽略旧请求的响应
    if (reqId !== myCouponRequestId) return
    if (res.code === 200) {
      // 后端返回 UserCouponVO 列表，字段直接在顶层（包含 title, discountAmount, couponType 等）
      const records = (res.data as any)?.records ?? (res.data as any) ?? []
      myCoupons.value = records
    }
  } catch (error) {
    // 竞态防护：忽略旧请求的错误
    if (reqId !== myCouponRequestId) return
    console.error('加载我的优惠券失败:', error)
  }
}

// 监听主tab切换
watch(activeTab, (val) => {
  if (val === 'available') {
    loadAvailableCoupons()
  } else if (val === 'my') {
    loadMyCoupons()
  }
})

// 监听子tab切换
watch(myCouponTab, () => {
  if (activeTab.value === 'my') {
    loadMyCoupons()
  }
})

// 领取优惠券
const handleReceive = async (coupon: any) => {
  const isVipFreeCoupon = coupon.vipFreeReceive === 1
  const pointsPrice = coupon.pointsPrice || 0

  // 只有勾选了VIP免积分，并且当前用户是VIP，才走免费领取
  if (isVipFreeCoupon && isVip.value) {
    try {
      const res = await receiveCoupon(coupon.id)
      if (res.code === 200) {
        ElMessage.success('领取成功')
        loadAvailableCoupons()
        loadPointsBalance()
      } else {
        ElMessage.error(res.message || '领取失败')
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '领取失败')
    }
    return
  }

  // 设置了兑换积分的券，VIP未免积分或普通用户都必须走积分兑换
  if (pointsPrice > 0) {
    if (userPoints.value < pointsPrice) {
      ElMessage.warning(`积分不足，需要${pointsPrice}积分，当前仅${userPoints.value}积分`)
      return
    }

    try {
      await ElMessageBox.confirm(
        `确认使用 ${pointsPrice} 积分兑换该优惠券？\n当前积分余额：${userPoints.value}`,
        '积分兑换确认',
        { confirmButtonText: '确认兑换', cancelButtonText: '取消', type: 'warning' }
      )
      const res = await exchangeCoupon(coupon.id)
      if (res.code === 200) {
        ElMessage.success('兑换成功！积分已扣除')
        loadAvailableCoupons()
        loadPointsBalance()
      } else {
        ElMessage.error(res.message || '兑换失败')
      }
    } catch (error: any) {
      if (error !== 'cancel' && error !== 'close') {
        ElMessage.error(error.response?.data?.message || '兑换失败')
      }
    }
    return
  }

  try {
    const res = await receiveCoupon(coupon.id)
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

// 判断优惠券是否已被领取
const isReceived = (couponId: number) => {
  return receivedCouponIds.value.includes(couponId)
}

// 获取优惠券获得方式：receive-领取，exchange-积分兑换
const getCouponAcquireType = (couponId: number) => {
  return couponAcquireTypeMap.value[String(couponId)] || 'receive'
}

// 获取按钮文本和状态
// 用户需求：兑换来的券显示"已兑换"，领取来的券显示"已领取"
const getCouponButtonState = (coupon: any) => {
  const remaining = (coupon.totalQuantity ?? 0) - (coupon.receivedQuantity ?? 0)

  if (remaining <= 0) {
    return { text: '已抢完', disabled: true }
  }

  if (isReceived(coupon.id)) {
    const acquiredByExchange = getCouponAcquireType(coupon.id) === 'exchange'
    return { text: acquiredByExchange ? '已兑换' : '已领取', disabled: true }
  }

  // VIP免费券区分显示
  if (coupon.vipFreeReceive === 1) {
    if (isVip.value) {
      return { text: 'VIP免费领', disabled: false }
    }
    // 非VIP：显示积分兑换
    const pointsPrice = coupon.pointsPrice || 0
    if (pointsPrice <= 0) {
      return { text: '暂不可兑换', disabled: true }
    }
    if (userPoints.value < pointsPrice) {
      return { text: `${pointsPrice}积分兑换`, disabled: true }
    }
    return { text: `${pointsPrice}积分兑换`, disabled: false }
  }

  if ((coupon.pointsPrice || 0) > 0) {
    if (userPoints.value < coupon.pointsPrice) {
      return { text: `${coupon.pointsPrice}积分兑换`, disabled: true }
    }
    return { text: `${coupon.pointsPrice}积分兑换`, disabled: false }
  }

  return { text: '领取', disabled: false }
}

onMounted(async () => {
  // 先查询VIP状态
  try {
    const vipRes = await getVipStatus()
    isVip.value = vipRes.data?.isVip || false
  } catch {
    isVip.value = false
  }
  // 加载积分余额（非VIP用户需要显示积分兑换信息）
  await loadPointsBalance()

  if (route.query.tab === 'my') {
    activeTab.value = 'my'
  } else {
    // 再加载优惠券
    loadAvailableCoupons()
  }
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
      background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);

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
