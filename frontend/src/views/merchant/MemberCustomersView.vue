<template>
  <div class="member-customers-page">
    <div class="header">
      <h2>会员客户</h2>
      <p class="desc">查看在本店有过消费记录的会员客户信息</p>
    </div>

    <el-card shadow="never">
      <el-table class="admin-data-table" :data="customerList" v-loading="loading" :fit="false" style="width: 100%">
        <el-table-column label="头像" width="58">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatar || undefined">
              <el-icon :size="20"><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="130" class-name="admin-ellipsis" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" width="130" class-name="admin-ellipsis" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="118" class-name="admin-nowrap">
          <template #default="{ row }">
            {{ row.phone || '未绑定' }}
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="96">
          <template #default="{ row }">
            <el-tag v-if="row.memberLevelName" type="warning">{{ row.memberLevelName }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="平台VIP" width="82">
          <template #default="{ row }">
            <el-tag v-if="row.isVip === 1" type="danger" effect="dark" size="small">
              <el-icon style="vertical-align: -2px;"><Star /></el-icon> VIP
            </el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="次数" width="62" />
        <el-table-column label="消费额" width="88">
          <template #default="{ row }">
            <span class="spend-amount">&yen;{{ (row.totalSpend ?? 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最近下单" width="168" class-name="admin-nowrap">
          <template #default="{ row }">
            {{ formatDateTime(row.lastOrderTime) }}
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
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const customerList = ref<Record<string, unknown>[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

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
