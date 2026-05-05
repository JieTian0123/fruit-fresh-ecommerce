<template>
  <div class="member-view">
    <!-- 顶部布局：会员信息 + VIP区域 -->
    <div class="top-dashboard-grid">
      <!-- 会员头部 -->
      <div class="member-header">
      <div class="member-info">
        <el-avatar :size="80" :src="userInfo?.avatar" />
        <div class="info-right">
          <h2>{{ userInfo?.nickname || userInfo?.username }}</h2>
          <div class="member-level">
            <el-tag v-if="vipStatus?.isVip" type="warning" size="large" effect="dark">
              VIP会员
            </el-tag>
            <el-tag v-else type="info" size="large">
              普通用户
            </el-tag>
          </div>
        </div>
      </div>

      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-value">{{ userInfo?.points || 0 }}</div>
          <div class="stat-label">积分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">¥{{ userInfo?.totalConsumption || '0.00' }}</div>
          <div class="stat-label">累计消费</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ signInStatus?.continuousDays || 0 }}</div>
          <div class="stat-label">连续签到(天)</div>
        </div>
      </div>

      <div class="actions-card">
        <div class="action-item" @click="$router.push('/member/sign-in')">
          <el-icon><Calendar /></el-icon>
          <span>签到记录</span>
        </div>
        <div class="action-item" @click="$router.push('/coupons?tab=my')">
          <el-icon><Ticket /></el-icon>
          <span>我的优惠券</span>
        </div>
        <div class="action-item" @click="$router.push('/follow-shops')">
          <el-icon><Shop /></el-icon>
          <span>我的关注 ({{ followCount }})</span>
        </div>
        <div class="action-item" @click="$router.push('/coupons')">
          <el-icon><Present /></el-icon>
          <span>积分商城</span>
        </div>
      </div>
    </div>

    <!-- VIP会员区域 -->
    <el-card class="vip-section" v-loading="vipLoading">
      <template #header>
        <div class="section-header">
          <h3><el-icon><Star /></el-icon> VIP会员</h3>
          <el-tag v-if="vipStatus?.isVip" type="warning" effect="dark" class="vip-badge-tag">
            <el-icon><Star /></el-icon> VIP会员
          </el-tag>
        </div>
      </template>

      <!-- VIP状态横幅 -->
      <div class="vip-banner" :class="{ 'vip-active': vipStatus?.isVip, 'vip-expired': !vipStatus?.isVip }">
        <div class="vip-banner-content">
          <div class="vip-banner-left">
            <div class="vip-icon-wrapper">
              <el-icon :size="36"><Star /></el-icon>
            </div>
            <div class="vip-banner-info">
              <h3 v-if="vipStatus?.isVip">尊敬的VIP会员</h3>
              <h3 v-else>开通VIP，畅享专属特权</h3>
              <p v-if="vipStatus?.isVip">
                有效期至：{{ formatVipExpireTime }}
              </p>
              <p v-else>完成订单或直接购买即可升级</p>
            </div>
          </div>
          <div class="vip-banner-right">
            <el-tag v-if="vipStatus?.isVip" type="warning" size="large" effect="dark" round>
              VIP生效中
            </el-tag>
            <el-tag v-else type="info" size="large" effect="plain" round>
              未开通
            </el-tag>
          </div>
        </div>
      </div>

      <!-- VIP权益亮点 -->
      <div class="vip-benefits-highlights">
        <div class="benefit-highlight-item">
          <div class="benefit-highlight-icon">
            <el-icon :size="28"><Ticket /></el-icon>
          </div>
          <div class="benefit-highlight-text">
            <h4>每周发放优惠券</h4>
            <p>VIP专属优惠券每周自动发放</p>
          </div>
        </div>
        <div class="benefit-highlight-item">
          <div class="benefit-highlight-icon">
            <el-icon :size="28"><Coin /></el-icon>
          </div>
          <div class="benefit-highlight-text">
            <h4>签到积分双倍</h4>
            <p>每日签到获得双倍积分奖励</p>
          </div>
        </div>
        <div class="benefit-highlight-item">
          <div class="benefit-highlight-icon">
            <el-icon :size="28"><Trophy /></el-icon>
          </div>
          <div class="benefit-highlight-text">
            <h4>专属身份标识</h4>
            <p>金色VIP徽章彰显尊贵身份</p>
          </div>
        </div>
      </div>

      <!-- 订单升级进度条 -->
      <div class="vip-upgrade-progress" v-if="vipStatus">
        <div class="upgrade-progress-header">
          <span class="upgrade-title">订单升级进度</span>
          <span class="upgrade-count">
            已完成 <strong>{{ vipStatus.completedOrders }}</strong>/{{ vipStatus.upgradeThreshold }} 笔订单
          </span>
        </div>
        <el-progress
          :percentage="vipUpgradeProgress"
          :stroke-width="14"
          :color="[
            { color: '#f6d365', percentage: 40 },
            { color: '#f0a030', percentage: 70 },
            { color: '#fda085', percentage: 100 }
          ]"
          :format="() => vipUpgradeProgress + '%'"
        />
        <p class="upgrade-hint" v-if="vipUpgradeProgress < 100">
          再完成 {{ vipStatus.upgradeThreshold - vipStatus.completedOrders }} 笔订单即可免费升级VIP
        </p>
        <p class="upgrade-hint upgrade-done" v-else>
          <el-icon><CircleCheckFilled /></el-icon> 已达成订单升级条件
        </p>
      </div>

      <!-- VIP套餐卡片 -->
      <div class="vip-plans" v-if="vipPlans.length > 0">
        <h4 class="vip-plans-title">开通VIP会员</h4>
        <div class="vip-plan-cards">
          <div class="vip-plan-card" v-for="plan in vipPlans" :key="plan.id">
            <div class="plan-header">
              <el-icon :size="22"><Star /></el-icon>
              <span class="plan-name">{{ plan.name }}</span>
            </div>
            <div class="plan-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ plan.price.toFixed(2) }}</span>
            </div>
            <div class="plan-duration">有效期 {{ plan.durationDays }} 天</div>
            <p class="plan-description" v-if="plan.description">{{ plan.description }}</p>
            <div class="plan-benefits" v-if="plan.benefits">
              <div v-for="(benefit, idx) in parseVipBenefits(plan.benefits)" :key="idx" class="plan-benefit-item">
                <el-icon><Check /></el-icon>
                <span>{{ benefit }}</span>
              </div>
            </div>
            <el-button
              type="warning"
              class="plan-buy-btn"
              :loading="purchaseLoading"
              @click="handlePurchaseVip(plan.id)"
              round
            >
              {{ vipStatus?.isVip ? '续费' : '立即开通' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
    </div>


    <div class="master-grid">
      <!-- 积分明细概览 (span 6) -->
      <div class="grid-card grid-card-full points-overview">
        <div class="points-header">
          <h3><el-icon><List /></el-icon> 最近积分变动</h3>
          <el-button link type="primary" @click="$router.push('/member/points-log')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="points-list" v-if="pointsLogList && pointsLogList.length > 0">
          <div class="points-item" v-for="item in pointsLogList.slice(0, 3)" :key="item.id">
            <div class="points-item-left">
              <span class="points-desc">{{ item.description }}</span>
              <span class="points-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="points-item-right" :class="item.points > 0 ? 'points-add' : 'points-deduct'">
              {{ item.points > 0 ? '+' : '' }}{{ item.points }}
            </div>
          </div>
        </div>
        <div v-else class="empty-points">暂无变动记录</div>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Ticket, Shop, Star, Check, Calendar, Coin, Trophy,
  CircleCheckFilled, Present
} from '@element-plus/icons-vue'
import { getCurrentUser } from '@/api/user'
import { getMyFollowShops } from '@/api/shop'
import { getVipStatus, getVipPlans, purchaseVip } from '@/api/vip'
import {
  signIn as signInApi,
  getSignInStatus,
  getPointsLog
} from '@/api/points'
import type { UserPointsLog, SignInStatus } from '@/types'
import type { VipPlan, VipStatus } from '@/types'
import { PointSourceTypeText } from '@/types'

const router = useRouter()

// 用户信息
const userInfo = ref<any>(null)

// 签到相关
const signInStatus = ref<SignInStatus | null>(null)
const signInLoading = ref(false)

// 关注数
const followCount = ref(0)
const loadFollowCount = async () => {
  try {
    const res = await getMyFollowShops()
    if (res.code === 200) {
      const data = res.data as any
      if (Array.isArray(data)) {
        followCount.value = data.length
      } else if (Array.isArray(data?.records)) {
        followCount.value = data.records.length
      } else if (Array.isArray(data?.list)) {
        followCount.value = data.list.length
      } else {
        const total = Number(data?.total)
        followCount.value = Number.isFinite(total) && total > 0 ? total : 0
      }
    }
  } catch (error) {
    console.error('加载关注数失败:', error)
  }
}

// 积分记录
const pointsLogList = ref<UserPointsLog[]>([])
const pointsLoading = ref(false)
const pointsPageNum = ref(1)
const pointsPageSize = ref(10)
const pointsTotal = ref(0)
const pointsLogVisible = ref(false)

// VIP会员
const vipStatus = ref<VipStatus | null>(null)
const vipPlans = ref<VipPlan[]>([])
const vipLoading = ref(false)
const purchaseLoading = ref(false)

// 计算今日可获得积分（已不使用，但保留以备需要）
const todayPointsEarned = computed(() => {
  const days = signInStatus.value?.continuousDays || 1
  const extra = Math.min((days - 1) * 2, 10)
  return 10 + extra
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载签到状态
const loadSignInStatus = async () => {
  try {
    const res = await getSignInStatus()
    if (res.code === 200) {
      signInStatus.value = res.data as SignInStatus
    }
  } catch (error) {
    console.error('加载签到状态失败:', error)
  }
}

// 加载积分记录
const loadPointsLog = async () => {
  pointsLoading.value = true
  try {
    const res = await getPointsLog(pointsPageNum.value, pointsPageSize.value)
    if (res.code === 200) {
      const data = res.data as any
      pointsLogList.value = data?.records || data?.list || []
      pointsTotal.value = data?.total || 0
    }
  } catch (error) {
    console.error('加载积分记录失败:', error)
  } finally {
    pointsLoading.value = false
  }
}

// 加载VIP状态
const loadVipStatus = async () => {
  try {
    const res = await getVipStatus()
    if (res.code === 200) {
      vipStatus.value = res.data as VipStatus
    }
  } catch (error) {
    console.error('加载VIP状态失败:', error)
  }
}

// 加载VIP套餐列表
const loadVipPlans = async () => {
  vipLoading.value = true
  try {
    const res = await getVipPlans()
    if (res.code === 200) {
      vipPlans.value = (res.data as VipPlan[]) || []
    }
  } catch (error) {
    console.error('加载VIP套餐失败:', error)
  } finally {
    vipLoading.value = false
  }
}

// 购买/续费VIP
const handlePurchaseVip = async (planId: number) => {
  const plan = vipPlans.value.find(p => p.id === planId)
  if (!plan) return
  const actionText = vipStatus.value?.isVip ? '续费' : '开通'
  try {
    await ElMessageBox.confirm(
      `确认${actionText}「${plan.name}」？价格：¥${plan.price.toFixed(2)}，有效期${plan.durationDays}天`,
      `${actionText}VIP会员`,
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning' }
    )
    purchaseLoading.value = true
    const res = await purchaseVip(planId)
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功！尊享VIP特权已生效`)
      await loadVipStatus()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('购买VIP失败:', error)
    }
  } finally {
    purchaseLoading.value = false
  }
}

// VIP升级进度百分比
const vipUpgradeProgress = computed(() => {
  if (!vipStatus.value) return 0
  const { completedOrders, upgradeThreshold } = vipStatus.value
  if (upgradeThreshold <= 0) return 100
  return Math.min(Math.round((completedOrders / upgradeThreshold) * 100), 100)
})

// 格式化VIP到期时间
const formatVipExpireTime = computed(() => {
  if (!vipStatus.value?.expireTime) return ''
  return vipStatus.value.expireTime.replace('T', ' ').substring(0, 10)
})

// 解析VIP套餐权益
const parseVipBenefits = (benefits: string | undefined): string[] => {
  if (!benefits) return []
  try {
    const parsed = JSON.parse(benefits)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

// 签到
const handleSignIn = async () => {
  if (signInStatus.value?.signedToday) return
  signInLoading.value = true
  try {
    const res = await signInApi()
    if (res.code === 200) {
      ElMessage.success('签到成功！')
      // 刷新数据（包括积分记录实时更新）
      await Promise.all([
        loadSignInStatus(),
        loadUserInfo(),
        loadPointsLog()
      ])
    }
  } catch (error) {
    // 错误已被拦截器处理
  } finally {
    signInLoading.value = false
  }
}

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 积分来源标签类型
const getSourceTagType = (type: number): string => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: '',
    5: 'danger',
    6: 'info',
    7: 'info'
  }
  return map[type] || 'info'
}

// 跳转到优惠券
const goToCoupons = () => {
  router.push('/coupons')
}

// 跳转到关注的店铺
const goToFollowShops = () => {
  router.push('/follow-shops')
}

onMounted(() => {
  loadUserInfo()
  loadSignInStatus()
  loadPointsLog()
  loadVipStatus()
  loadVipPlans()
  loadFollowCount()
})
</script>

<style scoped>
.member-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px;

  :deep(.el-card) {
    border: 1px solid rgba(244, 196, 48, 0.12);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
  }

  .top-dashboard-grid {
    display: grid;
    grid-template-columns: 340px 1fr;
    gap: 16px;
    margin-bottom: 16px;
    align-items: stretch;
  }

  .member-header {
    height: 100%;
    box-sizing: border-box;
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #fff8ea 0%, #fff1db 50%, #ffe8d0 100%);
    color: #5f370e;
    border: 1px solid rgba(240, 160, 48, 0.16);
    border-radius: 12px;
    padding: 24px 16px;
    margin-bottom: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;

    &::before,
    &::after {
      content: '';
      position: absolute;
      border-radius: 999px;
      pointer-events: none;
    }

    &::before {
      width: 220px;
      height: 220px;
      right: -70px;
      top: -120px;
      background: radial-gradient(circle, rgba(246, 211, 101, 0.34) 0%, rgba(246, 211, 101, 0) 70%);
    }

    &::after {
      width: 180px;
      height: 180px;
      left: -60px;
      bottom: -110px;
      background: radial-gradient(circle, rgba(253, 160, 133, 0.22) 0%, rgba(253, 160, 133, 0) 72%);
    }

    .member-info {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;

      :deep(.el-avatar) {
        width: 42px !important;
        height: 42px !important;
        border: 2px solid rgba(255, 255, 255, 0.9);
        box-shadow: 0 6px 12px rgba(240, 160, 48, 0.12);
      }

      .info-right {
        h2 {
          font-size: 16px;
          margin-bottom: 2px;
          color: #4a2d0b;
        }

        .member-level {
          .el-tag {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 999px;
          }
        }
      }
    }

    .stats-card {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      justify-content: space-around;
      gap: 8px;
      background: rgba(255, 255, 255, 0.85);
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.65);
      border-radius: 10px;
      padding: 12px 8px;
      margin-bottom: 12px;
      box-shadow: 0 2px 8px rgba(240, 160, 48, 0.05);

      .stat-item {
        flex: 1;
        text-align: center;

        .stat-value {
          font-size: 18px;
          font-weight: 800;
          margin-bottom: 2px;
          color: #7a4a12;
        }

        .stat-label {
          font-size: 12px;
          color: rgba(95, 55, 14, 0.8);
        }
      }
    }

    .actions-card {
      position: relative;
      z-index: 1;
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      background: rgba(255, 255, 255, 0.85);
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.65);
      border-radius: 10px;
      padding: 12px;
      box-shadow: 0 2px 8px rgba(240, 160, 48, 0.05);

      .action-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        border-radius: 8px;
        font-size: 13px;
        font-weight: bold;
        color: #7a4a12;
        cursor: pointer;
        transition: all 0.3s;
        background: rgba(255, 255, 255, 0.6);
        border: 1px solid rgba(255, 255, 255, 0.8);

        &:hover {
          background: #fff;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(240, 160, 48, 0.15);
        }

        .el-icon {
          font-size: 16px;
          color: #f0a030;
        }
      }
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    h3 {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      font-weight: bold;
      margin: 0;
    }

    .next-level-hint {
      font-size: 12px;
      color: #909399;
    }
  }

  .vip-section {
    height: 100%;
    box-sizing: border-box;
    margin-bottom: 0;
    border: 1px solid rgba(246, 211, 101, 0.22);
    overflow: hidden;
    display: flex;
    flex-direction: column;

    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
      border-bottom: none;
      padding: 8px 12px;

      .section-header h3 {
        color: #fff;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
      }

      .vip-badge-tag {
        font-weight: bold;
        letter-spacing: 1px;
      }
    }

    :deep(.el-card__body) {
      padding: 16px;
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .vip-banner {
      border-radius: 10px;
      padding: 10px 12px;
      margin-bottom: 8px;
      transition: all 0.3s;
      border: 1px solid rgba(255, 255, 255, 0.4);

      &.vip-active {
        background: linear-gradient(135deg, #f5c55d 0%, #f0a748 45%, #ee8b6f 100%);
        color: #fff;
        box-shadow: 0 12px 24px rgba(240, 160, 48, 0.15);
      }

      &.vip-expired {
        background: linear-gradient(135deg, #f6f2ea 0%, #fbfaf7 100%);
        color: #666;
      }

      .vip-banner-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .vip-banner-left {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .vip-icon-wrapper {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.24);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .el-icon {
          font-size: 15px !important;
        }
      }

      .vip-banner-info {
        h3 {
          font-size: 14px;
          font-weight: bold;
          margin: 0 0 2px 0;
        }

        p {
          margin: 0;
          font-size: 12px;
          opacity: 0.9;
        }
      }
    }

    .vip-benefits-highlights {
      display: flex;
      justify-content: space-between;
      gap: 8px;
      margin-bottom: 8px;

      .benefit-highlight-item {
        flex: 1;
        min-width: 0;
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 8px;
        background: linear-gradient(135deg, rgba(246, 211, 101, 0.1) 0%, rgba(253, 160, 133, 0.08) 100%);
        border-radius: 8px;
        border: 1px solid rgba(246, 211, 101, 0.16);
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(246, 211, 101, 0.15);
          border-color: rgba(246, 211, 101, 0.3);
        }

        .benefit-highlight-icon {
          width: 26px;
          height: 26px;
          border-radius: 6px;
          background: linear-gradient(135deg, #f2bb54 0%, #ef9a55 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          flex-shrink: 0;

          .el-icon {
            font-size: 14px !important;
          }
        }

        .benefit-highlight-text {
          min-width: 0;
          h4 {
            font-size: 12px;
            font-weight: 600;
            color: #333;
            margin: 0 0 2px 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          p {
            font-size: 10px;
            color: #8a8f99;
            margin: 0;
            line-height: 1.4;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
      }
    }

    .vip-upgrade-progress {
      background: linear-gradient(180deg, #fffdf8 0%, #fff 100%);
      border-radius: 8px;
      padding: 10px 12px;
      margin-bottom: 8px;
      border: 1px solid rgba(240, 160, 48, 0.1);

      .upgrade-progress-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        .upgrade-title {
          font-size: 12px;
          font-weight: 600;
          color: #333;
        }

        .upgrade-count {
          font-size: 11px;
          color: #666;

          strong {
            color: #f0a030;
            font-size: 12px;
          }
        }
      }

      .upgrade-hint {
        margin: 4px 0 0 0;
        font-size: 10px;
        color: #999;

        &.upgrade-done {
          color: #67C23A;
          display: flex;
          align-items: center;
          gap: 4px;
          font-weight: 500;
        }
      }
    }

    .vip-plans {
      .vip-plans-title {
        font-size: 13px;
        font-weight: 600;
        color: #333;
        margin: 0 0 8px 0;
      }

      .vip-plan-cards {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 12px;
      }

      .vip-plan-card {
        text-align: center;
        padding: 10px 10px;
        border: 2px solid rgba(246, 211, 101, 0.3);
        border-radius: 10px;
        background: linear-gradient(180deg, rgba(246, 211, 101, 0.04) 0%, #fff 100%);
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(246, 211, 101, 0.18);
          border-color: #f6d365;
        }

        .plan-header {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
          margin-bottom: 6px;
          color: #f0a030;

          .plan-name {
            font-size: 14px;
            font-weight: bold;
            color: #333;
          }
        }

        .plan-price {
          margin-bottom: 4px;

          .price-symbol {
            font-size: 12px;
            font-weight: bold;
            color: #f0a030;
            vertical-align: super;
          }

          .price-value {
            font-size: 20px;
            font-weight: bold;
            color: #f0a030;
            line-height: 1;
          }
        }

        .plan-duration {
          font-size: 12px;
          color: #999;
          margin-bottom: 6px;
        }

        .plan-description {
          font-size: 12px;
          color: #666;
          margin: 0 0 6px 0;
          line-height: 1.4;
        }

        .plan-benefits {
          text-align: left;
          margin-bottom: 8px;

          .plan-benefit-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: #666;
            margin-bottom: 4px;
          }
        }

        .plan-buy-btn {
          width: 100%;
          height: 30px;
          font-size: 12px;
          font-weight: 600;
          letter-spacing: 1px;
          transition: all 0.3s;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 6px 20px rgba(240, 160, 48, 0.4);
          }
        }
      }
    }
  }

  .points-add {
    color: #67C23A;
    font-weight: bold;
  }

  .points-deduct {
    color: #F56C6C;
    font-weight: bold;
  }

  @media (max-width: 992px) {
    .bento-container {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: auto;
    }
    .bento-card.tall {
      grid-row: auto;
      min-height: 180px;
    }
  }

  @media (max-width: 900px) {
    .top-dashboard-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 768px) {
    padding: 8px;

    .bento-container {
      grid-template-columns: 1fr;
    }

    .member-header {
      padding: 10px;

      .member-info {
        align-items: flex-start;
      }
    }

    .vip-section {
      .vip-banner {
        .vip-banner-content {
          flex-direction: column;
          align-items: flex-start;
          gap: 10px;
        }
      }
    }
  }
}

  :deep(.el-card) { border-radius: 12px !important; }
  .master-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .grid-card {
    background: #fff;
    border-radius: 12px;
    border: 1px solid rgba(244, 196, 48, 0.12);
    padding: 16px 20px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.02);
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
  }
  .grid-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px 0 rgba(0,0,0,0.06);
  }
  .grid-card-small {
    grid-column: span 1;
    gap: 16px;
  }
  .grid-card-full {
    grid-column: span 1;
    flex-direction: column;
    align-items: stretch;
    cursor: default;
  }
  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
  }
  .card-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  .card-info h4 { margin: 0 0 4px 0; font-size: 15px; font-weight: bold; color: #303133; }
  .card-info p { margin: 0; font-size: 12px; color: #909399; }
  .card-action {
    display: flex;
    align-items: center;
  }
  .arrow-icon { color: #c0c4cc; font-size: 18px; }
  .points-overview .points-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
  .points-overview .points-header h3 { margin: 0; font-size: 15px; display: flex; align-items: center; gap: 6px; color: #333; }
  .points-list { display: flex; flex-direction: column; gap: 12px; }
  .points-item { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f0f2f5; }
  .points-item:last-child { border-bottom: none; padding-bottom: 0; }
  .points-item-left { display: flex; flex-direction: column; gap: 4px; }
  .points-desc { font-size: 14px; color: #303133; }
  .points-time { font-size: 12px; color: #909399; }
  .empty-points { text-align: center; color: #999; padding: 20px; font-size: 13px; }
  .vip-section { margin-bottom: 16px; border-radius: 12px; }
  .vip-split-layout { gap: 16px; }
  .vip-right.promo-box { border-radius: 12px; }
  .member-header { border-radius: 12px; margin-bottom: 16px; }
  @media (max-width: 768px) {
    .grid-card-small { grid-column: span 1; }
  }

</style>
