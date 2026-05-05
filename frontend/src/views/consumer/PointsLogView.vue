<template>
  <div class="points-log-view">
    <div class="page-header">
      <h2 class="header-title">积分记录</h2>
    </div>

    <el-card class="points-card" shadow="never">
      <el-table :data="pointsLogList" v-loading="loading" stripe>
        <el-table-column label="时间" width="180">
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

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPointsLog"
          @size-change="loadPointsLog"
        />
      </div>
      <el-empty v-if="!loading && pointsLogList.length === 0" description="暂无积分记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPointsLog } from '@/api/points'
import type { UserPointsLog } from '@/types'
import { PointSourceTypeText } from '@/types'

const pointsLogList = ref<UserPointsLog[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const formatTime = (time: string): string => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

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

const loadPointsLog = async () => {
  loading.value = true
  try {
    const res = await getPointsLog(pageNum.value, pageSize.value)
    if (res.code === 200) {
      const data = res.data as any
      pointsLogList.value = data?.records || data?.list || []
      total.value = data?.total || 0
    }
  } catch (error) {
    console.error('加载积分记录失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPointsLog()
})
</script>

<style scoped>
.points-log-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.header-title {
  font-weight: bold;
  font-size: 20px;
  margin: 0;
}
.points-card {
  border-radius: 12px;
  border: 1px solid rgba(244, 196, 48, 0.12);
}
.points-add {
  color: #67C23A;
  font-weight: bold;
}
.points-deduct {
  color: #F56C6C;
  font-weight: bold;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>