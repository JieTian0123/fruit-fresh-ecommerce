<template>
  <div class="member-view">
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
      <div class="member-stats">
        <div class="stat-item">
          <div class="stat-value">{{ userInfo?.points || 0 }}</div>
          <div class="stat-label">积分</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">¥{{ userInfo?.totalConsumption || '0.00' }}</div>
          <div class="stat-label">累计消费</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ signInStatus?.continuousDays || 0 }}</div>
          <div class="stat-label">连续签到(天)</div>
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
        <div class="vip-plan-single">
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

    <!-- 签到区域 -->
    <el-card class="sign-in-section">
      <template #header>
        <div class="section-header">
          <h3><el-icon><Calendar /></el-icon> 每日签到</h3>
          <div class="sign-in-action">
            <span v-if="signInStatus?.signedToday" class="signed-text">
              <el-icon color="#67C23A"><CircleCheckFilled /></el-icon>
              今日已签到 +{{ todayPointsEarned }}积分
            </span>
            <el-button
              v-else
              type="primary"
              :loading="signInLoading"
              @click="handleSignIn"
            >
              立即签到
            </el-button>
            <el-tooltip placement="top">
              <template #content>
                <div>签到规则：</div>
                <div>基础：每日10积分</div>
                <div>连续签到额外奖励：(天数-1)×2，最多+10</div>
                <div>第1天=10分，第2天=12分，第6天+=20分</div>
                <div style="color: #f6d365; margin-top: 4px;">VIP用户签到积分双倍</div>
              </template>
              <el-icon class="tip-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>
      </template>
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell" :class="{ 'signed': isSignedDate(data.day) }">
            <span class="day-num">{{ getDayNum(data.day) }}</span>
            <span v-if="isSignedDate(data.day)" class="sign-mark">
              +{{ getSignedPoints(data.day) }}
            </span>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 功能卡片 -->
    <el-row :gutter="20" class="member-content">
      <el-col :span="8">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon" :size="24" color="#409EFF"><Ticket /></el-icon>
              <span>我的优惠券</span>
            </div>
          </template>
          <div class="card-content">
            <p>查看和使用您的优惠券</p>
            <el-button type="primary" @click="goToCoupons">查看优惠券</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon" :size="24" color="#67C23A"><Shop /></el-icon>
              <span>我的店铺</span>
            </div>
          </template>
          <div class="card-content">
            <p>管理您关注的店铺</p>
            <el-button type="success" @click="goToFollowShops">关注的店铺</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon" :size="24" color="#E6A23C"><Star /></el-icon>
              <span>积分商城</span>
            </div>
          </template>
          <div class="card-content">
            <p>使用积分兑换优惠券</p>
            <el-button type="warning" @click="$router.push('/point-exchange')">去兑换</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 积分记录 -->
    <el-card class="points-log-section">
      <template #header>
        <div class="section-header">
          <h3><el-icon><Coin /></el-icon> 积分记录</h3>
        </div>
      </template>
      <el-table :data="pointsLogList" v-loading="pointsLoading" stripe>
        <el-table-column label="时间" prop="createTime" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getSourceTagType(row.sourceType)">
              {{ PointSourceTypeText[row.sourceType] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" />
        <el-table-column label="积分变动" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'points-add' : 'points-deduct'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="余额" prop="balanceAfter" width="100" align="center" />
      </el-table>
      <div class="pagination" v-if="pointsTotal > 0">
        <el-pagination
          v-model:current-page="pointsPageNum"
          v-model:page-size="pointsPageSize"
          :total="pointsTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPointsLog"
          @size-change="loadPointsLog"
        />
      </div>
      <el-empty v-if="!pointsLoading && pointsLogList.length === 0" description="暂无积分记录" />
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Ticket, Shop, Star, Check, Calendar, Coin, Trophy,
  CircleCheckFilled, QuestionFilled
} from '@element-plus/icons-vue'
import { getCurrentUser } from '@/api/user'
import { getVipStatus, getVipPlans, purchaseVip } from '@/api/vip'
import {
  signIn as signInApi,
  getSignInStatus,
  getMonthSignIns,
  getPointsLog
} from '@/api/points'
import type { UserPointsLog, UserSignIn, SignInStatus } from '@/types'
import type { VipPlan, VipStatus } from '@/types'
import { PointSourceTypeText } from '@/types'

const router = useRouter()

// 用户信息
const userInfo = ref<any>(null)

// 签到相关
const signInStatus = ref<SignInStatus | null>(null)
const monthSignIns = ref<UserSignIn[]>([])
const signInLoading = ref(false)
const calendarDate = ref(new Date())

// 积分记录
const pointsLogList = ref<UserPointsLog[]>([])
const pointsLoading = ref(false)
const pointsPageNum = ref(1)
const pointsPageSize = ref(10)
const pointsTotal = ref(0)

// VIP会员
const vipStatus = ref<VipStatus | null>(null)
const vipPlans = ref<VipPlan[]>([])
const vipLoading = ref(false)
const purchaseLoading = ref(false)

// 计算今日可获得积分
const todayPointsEarned = computed(() => {
  const days = signInStatus.value?.continuousDays || 1
  const extra = Math.min((days - 1) * 2, 10)
  return 10 + extra
})

// 签到日期集合
const signedDateSet = computed(() => {
  const set = new Map<string, number>()
  monthSignIns.value.forEach(s => {
    set.set(s.signDate, s.pointsEarned)
  })
  return set
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

// 加载本月签到记录
const loadMonthSignIns = async () => {
  try {
    const date = calendarDate.value
    const res = await getMonthSignIns(date.getFullYear(), date.getMonth() + 1)
    if (res.code === 200) {
      monthSignIns.value = (res.data as UserSignIn[]) || []
    }
  } catch (error) {
    console.error('加载签到记录失败:', error)
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
  signInLoading.value = true
  try {
    const res = await signInApi()
    if (res.code === 200) {
      ElMessage.success('签到成功！')
      // 刷新数据
      await Promise.all([
        loadSignInStatus(),
        loadMonthSignIns(),
        loadUserInfo()
      ])
    }
  } catch (error) {
    // 错误已被拦截器处理
  } finally {
    signInLoading.value = false
  }
}

// 判断日期是否已签到
const isSignedDate = (day: string): boolean => {
  return signedDateSet.value.has(day)
}

// 获取签到日积分
const getSignedPoints = (day: string): number => {
  return signedDateSet.value.get(day) || 0
}

// 获取日期数字
const getDayNum = (day: string): string => {
  return day.split('-')[2]
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
  loadMonthSignIns()
  loadPointsLog()
  loadVipStatus()
  loadVipPlans()
})
</script>

<style scoped>
.member-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .member-header {
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    color: white;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 24px;

    .member-info {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 16px;

      .info-right {
        h2 {
          font-size: 22px;
          margin-bottom: 10px;
        }

        .member-level {
          .el-tag {
            font-size: 16px;
            padding: 8px 16px;
          }
        }
      }
    }

    .member-stats {
      display: flex;
      align-items: center;
      justify-content: space-around;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      padding: 20px;

      .stat-item {
        text-align: center;

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.9;
        }
      }

      .stat-divider {
        width: 1px;
        height: 40px;
        background: rgba(255, 255, 255, 0.3);
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
      gap: 8px;
      font-size: 18px;
      font-weight: bold;
      margin: 0;
    }

    .next-level-hint {
      font-size: 13px;
      color: #909399;
    }
  }

  .vip-section {
    margin-bottom: 24px;
    border: 1px solid rgba(246, 211, 101, 0.3);
    overflow: hidden;

    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
      border-bottom: none;

      .section-header h3 {
        color: #fff;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
      }

      .vip-badge-tag {
        font-weight: bold;
        letter-spacing: 1px;
      }
    }

    .vip-banner {
      border-radius: 12px;
      padding: 24px 28px;
      margin-bottom: 24px;
      transition: all 0.3s;

      &.vip-active {
        background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
        color: #fff;
      }

      &.vip-expired {
        background: linear-gradient(135deg, #e8e8e8 0%, #f5f5f5 100%);
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
        gap: 16px;
      }

      .vip-icon-wrapper {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.25);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .vip-banner-info {
        h3 {
          font-size: 22px;
          font-weight: bold;
          margin: 0 0 6px 0;
        }

        p {
          margin: 0;
          font-size: 14px;
          opacity: 0.85;
        }
      }
    }

    .vip-benefits-highlights {
      display: flex;
      gap: 20px;
      margin-bottom: 24px;

      .benefit-highlight-item {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 18px 20px;
        background: linear-gradient(135deg, rgba(246, 211, 101, 0.08) 0%, rgba(253, 160, 133, 0.08) 100%);
        border-radius: 10px;
        border: 1px solid rgba(246, 211, 101, 0.2);
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba(246, 211, 101, 0.2);
          border-color: rgba(246, 211, 101, 0.4);
        }

        .benefit-highlight-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          flex-shrink: 0;
        }

        .benefit-highlight-text {
          h4 {
            font-size: 15px;
            font-weight: 600;
            color: #333;
            margin: 0 0 4px 0;
          }

          p {
            font-size: 13px;
            color: #999;
            margin: 0;
          }
        }
      }
    }

    .vip-upgrade-progress {
      background: #fafafa;
      border-radius: 10px;
      padding: 20px 24px;
      margin-bottom: 24px;
      border: 1px solid #f0f0f0;

      .upgrade-progress-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .upgrade-title {
          font-size: 15px;
          font-weight: 600;
          color: #333;
        }

        .upgrade-count {
          font-size: 14px;
          color: #666;

          strong {
            color: #f0a030;
            font-size: 18px;
          }
        }
      }

      .upgrade-hint {
        margin: 10px 0 0 0;
        font-size: 13px;
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
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0 0 16px 0;
      }

      .vip-plan-single {
        display: flex;
        justify-content: center;

        .vip-plan-card {
          max-width: 360px;
          width: 100%;
        }
      }

      .vip-plan-card {
        text-align: center;
        padding: 28px 20px;
        border: 2px solid rgba(246, 211, 101, 0.3);
        border-radius: 12px;
        background: linear-gradient(180deg, rgba(246, 211, 101, 0.04) 0%, #fff 100%);
        transition: all 0.3s;

        &:hover {
          transform: translateY(-6px);
          box-shadow: 0 8px 24px rgba(246, 211, 101, 0.25);
          border-color: #f6d365;
        }

        .plan-header {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          margin-bottom: 16px;
          color: #f0a030;

          .plan-name {
            font-size: 18px;
            font-weight: bold;
            color: #333;
          }
        }

        .plan-price {
          margin-bottom: 8px;

          .price-symbol {
            font-size: 18px;
            font-weight: bold;
            color: #f0a030;
            vertical-align: super;
          }

          .price-value {
            font-size: 36px;
            font-weight: bold;
            color: #f0a030;
            line-height: 1;
          }
        }

        .plan-duration {
          font-size: 14px;
          color: #999;
          margin-bottom: 14px;
        }

        .plan-description {
          font-size: 13px;
          color: #666;
          margin: 0 0 14px 0;
          line-height: 1.6;
        }

        .plan-benefits {
          text-align: left;
          margin-bottom: 18px;

          .plan-benefit-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: #666;
            margin-bottom: 6px;

            .el-icon {
              color: #f0a030;
              flex-shrink: 0;
            }
          }
        }

        .plan-buy-btn {
          width: 100%;
          font-size: 15px;
          font-weight: 600;
          letter-spacing: 1px;
        }
      }
    }
  }

  .sign-in-section {
    margin-bottom: 24px;

    .sign-in-action {
      display: flex;
      align-items: center;
      gap: 12px;

      .signed-text {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #67C23A;
        font-weight: 500;
      }

      .tip-icon {
        cursor: pointer;
        color: #909399;
        font-size: 18px;
      }
    }

    .calendar-cell {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .day-num {
        font-size: 14px;
      }

      .sign-mark {
        font-size: 11px;
        color: #67C23A;
        font-weight: bold;
      }

      &.signed {
        background: rgba(103, 194, 58, 0.1);
        border-radius: 4px;

        .day-num {
          color: #67C23A;
          font-weight: bold;
        }
      }
    }
  }

  .member-content {
    margin-bottom: 24px;

    .feature-card {
      .card-header {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 18px;
        font-weight: bold;

        .header-icon {
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .card-content {
        text-align: center;
        padding: 20px 0;

        p {
          color: #666;
          margin-bottom: 20px;
        }
      }
    }
  }

  .points-log-section {
    margin-bottom: 24px;

    .points-add {
      color: #67C23A;
      font-weight: bold;
    }

    .points-deduct {
      color: #F56C6C;
      font-weight: bold;
    }

    .pagination {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }
  }

}
</style>
