<template>
  <div class="freshness-bar" v-if="shelfLifeDays && productionDate">
    <div class="freshness-header">
      <span class="freshness-label">
        <el-icon><Timer /></el-icon>
        新鲜度
      </span>
      <span class="freshness-days" :style="{ color: statusColor }">
        {{ remainingText }}
      </span>
    </div>
    <el-progress
      :percentage="freshnessPercent"
      :color="progressColor"
      :stroke-width="strokeWidth"
      :show-text="false"
    />
    <div class="freshness-meta" v-if="showMeta">
      <span class="meta-item" v-if="qualityGrade">
        <el-tag size="small" :type="gradeTagType">{{ qualityGrade }}级</el-tag>
      </span>
      <span class="meta-item" v-if="storageCondition">
        🧊 {{ storageCondition }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Timer } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  productionDate?: string
  shelfLifeDays?: number
  qualityGrade?: string
  storageCondition?: string
  showMeta?: boolean
  strokeWidth?: number
}>(), {
  showMeta: false,
  strokeWidth: 8
})

const remainingDays = computed(() => {
  if (!props.productionDate || !props.shelfLifeDays) return -1
  const production = new Date(props.productionDate)
  const now = new Date()
  const diffMs = now.getTime() - production.getTime()
  const daysPassed = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const remaining = props.shelfLifeDays - daysPassed
  return remaining < 0 ? 0 : remaining
})

const freshnessPercent = computed(() => {
  if (!props.shelfLifeDays || props.shelfLifeDays <= 0) return 0
  const percent = (remainingDays.value / props.shelfLifeDays) * 100
  return Math.max(0, Math.min(100, Math.round(percent)))
})

const statusColor = computed(() => {
  if (freshnessPercent.value > 70) return '#10b981'
  if (freshnessPercent.value > 30) return '#f59e0b'
  return '#ef4444'
})

const progressColor = computed(() => {
  if (freshnessPercent.value > 70) return '#10b981'
  if (freshnessPercent.value > 30) return '#f59e0b'
  return '#ef4444'
})

const remainingText = computed(() => {
  if (remainingDays.value <= 0) return '已过期'
  if (remainingDays.value === 1) return '仅剩1天'
  return `剩余${remainingDays.value}天`
})

const gradeTagType = computed(() => {
  if (props.qualityGrade === 'A') return 'success'
  if (props.qualityGrade === 'B') return 'warning'
  return 'info'
})
</script>

<style scoped>
.freshness-bar {
  padding: 8px 0;
}

.freshness-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.freshness-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.freshness-days {
  font-size: 13px;
  font-weight: 600;
}

.freshness-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}
</style>