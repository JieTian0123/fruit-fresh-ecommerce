<template>
  <div class="point-exchange-view">
    <div class="exchange-header">
      <h2>积分兑换</h2>
      <div class="points-balance">
        <el-icon><Coin /></el-icon>
        <span>当前积分：</span>
        <span class="balance-value">{{ pointsBalance }}</span>
      </div>
    </div>

    <div v-loading="loading" class="coupon-list">
      <el-empty v-if="!loading && couponList.length === 0" description="暂无可兑换的优惠券" />
      <div v-else class="coupon-grid">
        <div v-for="coupon in couponList" :key="coupon.id" class="coupon-card">
          <div class="coupon-left">
            <div class="coupon-amount">
              <template v-if="coupon.couponType === 2">
                <span class="value">{{ coupon.discountRate * 10 }}</span>
                <span class="symbol">折</span>
              </template>
              <template v-else>
                <span class="symbol">¥</span>
                <span class="value">{{ coupon.discountAmount }}</span>
              </template>
            </div>
            <div class="coupon-condition">
              {{ coupon.minimumAmount ? `满¥${coupon.minimumAmount}可用` : '无门槛' }}
            </div>
            <el-tag
              :type="couponTypeTag(coupon.couponType).type"
              size="small"
              class="coupon-type-tag"
            >
              {{ couponTypeTag(coupon.couponType).label }}
            </el-tag>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.title }}</div>
            <div class="coupon-desc" v-if="coupon.description">{{ coupon.description }}</div>
            <div v-if="coupon.couponType === 2 && coupon.maximumDiscount" class="coupon-max-discount">
              最多优惠¥{{ coupon.maximumDiscount }}
            </div>
            <div class="coupon-stock">
              剩余 {{ coupon.totalQuantity - coupon.receivedQuantity }} 张
            </div>
            <div class="coupon-bottom">
              <div class="points-price">
                <el-icon><Coin /></el-icon>
                <span>{{ coupon.pointsPrice }}积分</span>
              </div>
              <el-button
                type="primary"
                size="small"
                :disabled="getExchangeButtonState(coupon).disabled"
                @click="handleExchange(coupon)"
              >
                {{ getExchangeButtonState(coupon).text }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadCoupons"
        @size-change="loadCoupons"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin } from '@element-plus/icons-vue'
import { getExchangeableCoupons, exchangeCoupon, getCouponAcquireTypes } from '@/api/coupon'
import { getPointsBalance } from '@/api/points'

const loading = ref(false)
const couponList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const pointsBalance = ref(0)
const couponAcquireTypeMap = ref<Record<string, 'receive' | 'exchange'>>({})

// 优惠券类型标签
const couponTypeTag = (type: number) => {
  const map: Record<number, { label: string; type: string }> = {
    1: { label: '满减券', type: 'warning' },
    2: { label: '折扣券', type: 'danger' },
    3: { label: '无门槛券', type: 'success' }
  }
  return map[type] || { label: '优惠券', type: 'info' }
}

// 加载积分余额
const loadBalance = async () => {
  try {
    const res = await getPointsBalance()
    if (res.code === 200) {
      pointsBalance.value = res.data || 0
    }
  } catch (error) {
    console.error('加载积分余额失败:', error)
  }
}

const loadCouponAcquireTypes = async () => {
  try {
    const res = await getCouponAcquireTypes()
    if (res.code === 200) {
      couponAcquireTypeMap.value = res.data || {}
    }
  } catch (error) {
    console.error('加载优惠券获得方式失败:', error)
  }
}

// 加载可兑换优惠券列表
const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await getExchangeableCoupons(pageNum.value, pageSize.value)
    if (res.code === 200) {
      const data = res.data
      couponList.value = data?.records || []
      total.value = data?.total || 0
    }
  } catch (error) {
    console.error('加载可兑换优惠券失败:', error)
  } finally {
    loading.value = false
  }
}

const getCouponAcquireType = (couponId: number) => {
  return couponAcquireTypeMap.value[String(couponId)]
}

const getExchangeButtonState = (coupon: any) => {
  const acquireType = getCouponAcquireType(coupon.id)
  if (acquireType === 'exchange') {
    return { text: '已兑换', disabled: true }
  }
  if (acquireType === 'receive') {
    return { text: '已领取', disabled: true }
  }
  if (coupon.totalQuantity - coupon.receivedQuantity <= 0) {
    return { text: '已兑完', disabled: true }
  }
  if (pointsBalance.value < coupon.pointsPrice) {
    return { text: '积分不足', disabled: true }
  }
  return { text: '立即兑换', disabled: false }
}

// 兑换优惠券
const handleExchange = async (coupon: any) => {
  if (getExchangeButtonState(coupon).disabled) return
  try {
    await ElMessageBox.confirm(
      `确定使用 ${coupon.pointsPrice} 积分兑换「${coupon.title}」吗？`,
      '确认兑换',
      {
        confirmButtonText: '确定兑换',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    const res = await exchangeCoupon(coupon.id)
    if (res.code === 200) {
      ElMessage.success('兑换成功')
      couponAcquireTypeMap.value[String(coupon.id)] = 'exchange'
      await Promise.all([loadBalance(), loadCoupons(), loadCouponAcquireTypes()])
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error(error.response?.data?.message || '兑换失败')
  }
}

onMounted(() => {
  loadBalance()
  loadCoupons()
  loadCouponAcquireTypes()
})
</script>

<style scoped>
.point-exchange-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .exchange-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30px;

    h2 {
      font-size: 24px;
      font-weight: bold;
      margin: 0;
    }

    .points-balance {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 16px;
      color: #666;
      background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(230, 162, 60, 0.05));
      padding: 10px 20px;
      border-radius: 8px;

      .el-icon {
        color: #E6A23C;
        font-size: 20px;
      }

      .balance-value {
        font-size: 24px;
        font-weight: bold;
        color: #E6A23C;
      }
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
        }
      }

      .coupon-condition {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
      }

      .coupon-type-tag {
        border: none;
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

      .coupon-max-discount {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }

      .coupon-stock {
        font-size: 12px;
        color: #999;
        margin-bottom: 12px;
      }

      .coupon-bottom {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .points-price {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 18px;
          font-weight: bold;
          color: #E6A23C;

          .el-icon {
            font-size: 20px;
          }
        }
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
