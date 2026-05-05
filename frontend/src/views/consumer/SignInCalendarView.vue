<template>
  <div class="sign-in-calendar-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" text @click="$router.back()">返回</el-button>
      </div>
      <h2>签到记录</h2>
      <div class="header-right">
        <span class="continuous-days">
          连续签到 <strong>{{ signInStatus?.continuousDays || 0 }}</strong> 天
        </span>
      </div>
    </div>

    <!-- 签到操作区 -->
    <el-card class="sign-in-action-card">
      <div class="action-content">
        <div class="action-left">
          <div class="today-info">
            <span class="today-label">今日签到可获得</span>
            <span class="today-points">+{{ todayPointsEarned }} 积分</span>
            <el-tag v-if="vipStatus?.isVip" type="warning" size="small" effect="dark" class="vip-bonus">
              VIP双倍
            </el-tag>
          </div>
          <div class="sign-rules">
            <el-tooltip placement="bottom">
              <template #content>
                <div>签到规则：</div>
                <div>基础：每日10积分</div>
                <div>连续签到额外奖励：(天数-1)×2，最多+10</div>
                <div>第1天=10分，第2天=12分，第6天+=20分</div>
                <div style="color: #f6d365; margin-top: 4px;">VIP用户签到积分双倍</div>
              </template>
              <span class="rules-link"><el-icon><QuestionFilled /></el-icon> 签到规则</span>
            </el-tooltip>
          </div>
        </div>
        <div class="action-right">
          <span v-if="signInStatus?.signedToday" class="signed-text">
            <el-icon color="#67C23A" :size="20"><CircleCheckFilled /></el-icon>
            今日已签到
          </span>
          <el-button
            v-else
            type="primary"
            size="large"
            :loading="signInLoading"
            @click="handleSignIn"
          >
            立即签到
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 日历区域 -->
    <el-card class="calendar-card">
      <el-calendar v-model="calendarDate" @update:model-value="handleMonthChange">
        <template #date-cell="{ data }">
          <div class="calendar-cell" :class="{ 'signed': isSignedDate(data.day), 'today': isToday(data.day) }">
            <span class="day-num">{{ getDayNum(data.day) }}</span>
            <span v-if="isSignedDate(data.day)" class="sign-mark">
              <el-icon color="#67C23A"><Check /></el-icon>
              +{{ getSignedPoints(data.day) }}
            </span>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 本月签到统计 -->
    <el-card class="stats-card">
      <template #header>
        <span>本月签到统计</span>
      </template>
      <div class="stats-content">
        <div class="stat-item">
          <div class="stat-value">{{ monthSignIns.length }}</div>
          <div class="stat-label">签到天数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ totalMonthPoints }}</div>
          <div class="stat-label">获得积分</div>
        </div>
      </div>
    </el-card>

    <!-- 本月签到明细 -->
    <el-card class="records-card">
      <template #header>
        <span>本月签到明细</span>
      </template>
      <div v-if="monthSignIns.length > 0" class="sign-record-list">
        <div
          v-for="record in monthSignIns"
          :key="record.id || record.signDate"
          class="sign-record-item"
        >
          <div class="record-date">
            <span class="record-day">{{ formatSignDate(record.signDate) }}</span>
            <span class="record-week">{{ getWeekday(record.signDate) }}</span>
          </div>
          <div class="record-streak">
            连续签到 {{ record.continuousDays }} 天
          </div>
          <div class="record-points">
            +{{ record.pointsEarned }} 积分
          </div>
        </div>
      </div>
      <el-empty v-else description="本月暂无签到记录" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, CircleCheckFilled, QuestionFilled } from '@element-plus/icons-vue'
import { getVipStatus } from '@/api/vip'
import {
  signIn as signInApi,
  getSignInStatus,
  getMonthSignIns
} from '@/api/points'
import type { UserSignIn, SignInStatus } from '@/types'
import type { VipStatus } from '@/types'

// 签到相关
const signInStatus = ref<SignInStatus | null>(null)
const monthSignIns = ref<UserSignIn[]>([])
const signInLoading = ref(false)
const calendarDate = ref(new Date())

// VIP状态
const vipStatus = ref<VipStatus | null>(null)

// 计算今日可获得积分
const todayPointsEarned = computed(() => {
  const days = signInStatus.value?.continuousDays || 1
  const extra = Math.min((days - 1) * 2, 10)
  let base = 10 + extra
  if (vipStatus.value?.isVip) {
    base *= 2
  }
  return base
})

// 本月总积分
const totalMonthPoints = computed(() => {
  return monthSignIns.value.reduce((sum, s) => sum + s.pointsEarned, 0)
})

// 签到日期集合
const signedDateSet = computed(() => {
  const set = new Map<string, number>()
  monthSignIns.value.forEach(s => {
    set.set(s.signDate, s.pointsEarned)
  })
  return set
})

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

// 签到
const handleSignIn = async () => {
  signInLoading.value = true
  try {
    const res = await signInApi()
    if (res.code === 200) {
      ElMessage.success('签到成功！')
      await Promise.all([
        loadSignInStatus(),
        loadMonthSignIns()
      ])
    }
  } catch (error) {
    // 错误已被拦截器处理
  } finally {
    signInLoading.value = false
  }
}

// 月份切换
const handleMonthChange = () => {
  loadMonthSignIns()
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
  return day.split('-')[2] || ''
}

const formatSignDate = (day: string): string => {
  if (!day) return ''
  return day.replace(/-/g, '.')
}

const getWeekday = (day: string): string => {
  if (!day) return ''
  const [yearText, monthText, dateText] = day.split('-')
  const year = Number(yearText)
  const month = Number(monthText)
  const date = Number(dateText)
  if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(date)) return ''
  const weekday = new Date(year, month - 1, date).getDay()
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][weekday] || ''
}

// 判断是否今天
const isToday = (day: string): boolean => {
  const today = new Date().toISOString().split('T')[0]
  return day === today
}

// 监听日历日期变化
watch(calendarDate, () => {
  loadMonthSignIns()
})

onMounted(() => {
  loadSignInStatus()
  loadMonthSignIns()
  loadVipStatus()
})
</script>

<style scoped>
.sign-in-calendar-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    padding: 8px 0;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }

    .continuous-days {
      font-size: 14px;
      color: #666;

      strong {
        color: #f0a030;
        font-size: 18px;
      }
    }
  }

  .sign-in-action-card {
    margin-bottom: 16px;
    border: 1px solid rgba(103, 194, 58, 0.2);
    border-radius: 12px;

    :deep(.el-card__body) {
      padding: 16px;
    }

    .action-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .action-left {
      .today-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .today-label {
          font-size: 14px;
          color: #666;
        }

        .today-points {
          font-size: 20px;
          font-weight: bold;
          color: #67C23A;
        }

        .vip-bonus {
          font-size: 10px;
        }
      }

      .sign-rules {
        .rules-link {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #909399;
          cursor: pointer;

          &:hover {
            color: #409EFF;
          }
        }
      }
    }

    .action-right {
      .signed-text {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 16px;
        font-weight: 500;
        color: #67C23A;
      }
    }
  }

  .calendar-card {
    margin-bottom: 16px;
    border-radius: 12px;

    :deep(.el-card__body) {
      padding: 8px;
    }

    :deep(.el-calendar) {
      .el-calendar__header {
        padding: 8px 16px;
      }

      .el-calendar__body {
        padding: 8px;
      }

      .el-calendar-table .el-calendar-day {
        height: 60px;
        padding: 4px;
      }

      .el-calendar-table thead th {
        padding: 8px 0;
        font-size: 13px;
      }
    }

    .calendar-cell {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      transition: all 0.2s;

      .day-num {
        font-size: 14px;
        font-weight: 500;
      }

      .sign-mark {
        display: flex;
        align-items: center;
        gap: 2px;
        font-size: 11px;
        color: #67C23A;
        font-weight: bold;
      }

      &.today {
        background: rgba(64, 158, 255, 0.1);
        border: 1px solid rgba(64, 158, 255, 0.3);

        .day-num {
          color: #409EFF;
        }
      }

      &.signed {
        background: rgba(103, 194, 58, 0.12);

        .day-num {
          color: #67C23A;
          font-weight: bold;
        }
      }

      &.signed.today {
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.15) 100%);
        border: 1px solid rgba(103, 194, 58, 0.4);
      }
    }
  }

  .stats-card {
    border-radius: 12px;

    :deep(.el-card__header) {
      padding: 12px 16px;
      font-weight: 600;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }

    .stats-content {
      display: flex;
      justify-content: space-around;

      .stat-item {
        text-align: center;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #67C23A;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .records-card {
    border-radius: 12px;

    :deep(.el-card__header) {
      padding: 12px 16px;
      font-weight: 600;
    }

    :deep(.el-card__body) {
      padding: 12px 16px;
    }

    .sign-record-list {
      display: grid;
      gap: 10px;
    }

    .sign-record-item {
      display: grid;
      grid-template-columns: 180px 1fr auto;
      align-items: center;
      gap: 12px;
      padding: 12px 14px;
      border-radius: 10px;
      background: linear-gradient(135deg, rgba(103, 194, 58, 0.08) 0%, rgba(240, 160, 48, 0.06) 100%);
      border: 1px solid rgba(103, 194, 58, 0.14);
    }

    .record-date {
      display: flex;
      align-items: baseline;
      gap: 8px;
      min-width: 0;
    }

    .record-day {
      font-size: 15px;
      font-weight: 700;
      color: #303133;
    }

    .record-week,
    .record-streak {
      font-size: 13px;
      color: #909399;
    }

    .record-points {
      font-size: 15px;
      font-weight: 700;
      color: #67C23A;
      white-space: nowrap;
    }
  }
}

@media (max-width: 600px) {
  .sign-in-calendar-view {
    padding: 12px;

    .page-header {
      flex-wrap: wrap;
      gap: 8px;

      .header-left {
        order: 1;
      }

      h2 {
        order: 2;
        flex: 1;
        text-align: center;
      }

      .header-right {
        order: 3;
        width: 100%;
        text-align: center;
      }
    }

    .sign-in-action-card {
      .action-content {
        flex-direction: column;
        gap: 16px;
        text-align: center;
      }
    }

    .calendar-card {
      :deep(.el-calendar-table .el-calendar-day) {
        height: 50px;
      }

      .calendar-cell {
        .sign-mark {
          font-size: 10px;
        }
      }
    }

    .records-card {
      .sign-record-item {
        grid-template-columns: 1fr;
        align-items: flex-start;
      }
    }
  }
}
</style>
