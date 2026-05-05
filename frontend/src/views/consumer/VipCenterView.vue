<template>
  <div class="vip-center-view">
    <el-page-header @back="$router.back()" title="返回" class="page-header">
      <template #content>
        <span class="header-title">VIP 会员特权中心</span>
      </template>
    </el-page-header>

    <div v-loading="loading">
      <div class="vip-intro">
        <el-icon class="vip-icon"><Trophy /></el-icon>
        <h2>尊享特权，畅享品质生活</h2>
        <p>开通 VIP，即可享受全场商品折上折、积分双倍、每周专享福利等尊贵权益。</p>
      </div>

      <div class="vip-plans" v-if="vipPlans.length > 0">
        <h3 class="plans-title">选择您的专属套餐</h3>
        <div class="vip-plan-cards">
          <div class="vip-plan-card" v-for="plan in vipPlans" :key="plan.id">
            <div class="plan-header">
              <el-icon :size="24"><Star /></el-icon>
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
              type="danger"
              class="plan-buy-btn"
              :loading="purchaseLoading"
              @click="handlePurchaseVip(plan.id)"
              round
            >
              {{ isVip ? '立即续费' : '立即开通' }}
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无可用套餐" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, Star, Check } from '@element-plus/icons-vue'
import { getVipStatus, getVipPlans, purchaseVip } from '@/api/vip'
import type { VipPlan, VipStatus } from '@/types'

const loading = ref(true)
const purchaseLoading = ref(false)
const vipStatus = ref<VipStatus | null>(null)
const vipPlans = ref<VipPlan[]>([])

const isVip = computed(() => vipStatus.value?.isVip ?? false)

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

const loadData = async () => {
  loading.value = true
  try {
    const [statusRes, plansRes] = await Promise.all([
      getVipStatus(),
      getVipPlans()
    ])
    if (statusRes.code === 200) vipStatus.value = statusRes.data as VipStatus
    if (plansRes.code === 200) vipPlans.value = (plansRes.data as VipPlan[]) || []
  } catch (error) {
    console.error('加载VIP中心失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePurchaseVip = async (planId: number) => {
  const plan = vipPlans.value.find(p => p.id === planId)
  if (!plan) return
  const actionText = isVip.value ? '续费' : '开通'
  try {
    await ElMessageBox.confirm(
      `确认${actionText}「${plan.name}」？\n价格：¥${plan.price.toFixed(2)}\n有效期：${plan.durationDays}天`,
      `${actionText}VIP会员`,
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning' }
    )
    purchaseLoading.value = true
    const res = await purchaseVip(planId)
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功！尊享VIP特权已生效`)
      await loadData()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('购买VIP失败:', error)
    }
  } finally {
    purchaseLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.vip-center-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-header { margin-bottom: 24px; }
.header-title { font-weight: bold; font-size: 18px; }

.vip-intro {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #2b2b2b 0%, #1a1a1a 100%);
  border-radius: 16px;
  color: #f6d365;
  margin-bottom: 30px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}
.vip-icon { font-size: 64px; margin-bottom: 16px; color: #f6d365; }
.vip-intro h2 { margin: 0 0 12px 0; font-size: 28px; font-weight: bold; letter-spacing: 2px; }
.vip-intro p { margin: 0; color: #ccc; font-size: 14px; }

.plans-title { font-size: 20px; font-weight: 800; color: #333; margin-bottom: 20px; text-align: center; }

.vip-plan-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}
.vip-plan-card {
  text-align: center;
  padding: 30px 20px;
  border: 2px solid rgba(246, 211, 101, 0.3);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(246, 211, 101, 0.04) 0%, #fff 100%);
  transition: all 0.3s;
}
.vip-plan-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(246, 211, 101, 0.2);
  border-color: #f6d365;
}
.plan-header {
  display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 12px; color: #f0a030;
}
.plan-name { font-size: 18px; font-weight: bold; color: #333; }
.plan-price { margin-bottom: 8px; }
.price-symbol { font-size: 16px; font-weight: bold; color: #d9393b; vertical-align: super; }
.price-value { font-size: 36px; font-weight: 800; color: #d9393b; line-height: 1; }
.plan-duration { font-size: 13px; color: #999; margin-bottom: 16px; }
.plan-description { font-size: 13px; color: #666; margin-bottom: 16px; line-height: 1.5; }
.plan-benefits { text-align: left; margin-bottom: 24px; padding: 0 10px; }
.plan-benefit-item { display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #555; margin-bottom: 8px; line-height: 1.4; }
.plan-benefit-item .el-icon { color: #67C23A; margin-top: 3px; }
.plan-buy-btn { width: 100%; height: 44px; font-size: 16px; font-weight: bold; letter-spacing: 2px; }
</style>