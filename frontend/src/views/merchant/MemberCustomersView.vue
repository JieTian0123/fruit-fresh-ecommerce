<template>
  <div class="member-customers-page">
    <div class="header">
      <h2>会员客户</h2>
      <p class="desc">查看在本店有过消费记录的会员客户信息</p>
    </div>

    <el-card shadow="never">
      <el-table :data="customerList" v-loading="loading" style="width: 100%">
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar || undefined">
              <el-icon :size="20"><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" min-width="130">
          <template #default="{ row }">
            {{ row.phone || '未绑定' }}
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.memberLevelName" type="warning">{{ row.memberLevelName }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="平台VIP" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isVip === 1" type="danger" effect="dark" size="small">
              <el-icon style="vertical-align: -2px;"><Star /></el-icon> VIP
            </el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="下单次数" width="100" />
        <el-table-column label="消费总额" width="120">
          <template #default="{ row }">
            <span class="spend-amount">&yen;{{ (row.totalSpend ?? 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最近下单" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastOrderTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadCustomers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMemberCustomers } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const loading = ref(false)
const customerList = ref<Record<string, unknown>[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return '-'
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

async function loadCustomers() {
  loading.value = true
  try {
    const res = await getMemberCustomers(pageNum.value, pageSize.value)
    if (res.data) {
      customerList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取会员客户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.member-customers-page {
  max-width: 1200px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.header .desc {
  margin: 0;
  color: var(--color-text-secondary, #909399);
  font-size: 14px;
}

.spend-amount {
  color: #f56c6c;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
