<template>
  <div class="merchant-list-page">
    <div class="header">
      <h2>商家管理</h2>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="用户名/昵称" clearable style="width: 200px" @keyup.enter="loadMerchants" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
        <el-option label="待审核" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadMerchants">搜索</el-button>
    </div>
    
    <!-- 商家表格 -->
    <div class="admin-table-shell is-medium">
      <el-table class="admin-data-table" :data="merchantList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column label="商家信息" min-width="180">
          <template #default="{ row }">
            <div class="shop-cell">
              <el-avatar :size="32" :src="normalizeImageUrl(row.avatar, defaultAvatar)">{{ row.nickname?.[0] || row.username?.[0] }}</el-avatar>
              <div class="shop-info">
                <p class="name">{{ row.nickname || row.username }}</p>
                <p class="contact">{{ row.phone || '-' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" class-name="admin-ellipsis" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="76">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="168" class-name="admin-nowrap">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="168" class-name="admin-action-column">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button text type="primary" @click="handleView(row)">查看</el-button>
              <el-button v-if="row.status === 2" text type="success" @click="handleAudit(row, 1)">通过</el-button>
              <el-button v-if="row.status === 2" text type="danger" @click="handleAudit(row, 0)">拒绝</el-button>
              <el-button v-if="row.status === 1" text type="danger" @click="handleToggleStatus(row, 0)">禁用</el-button>
              <el-button v-if="row.status === 0" text type="success" @click="handleToggleStatus(row, 1)">启用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadMerchants"
      />
    </div>
    
    <!-- 商家详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="商家详情" size="500px">
      <template v-if="currentMerchant">
        <div class="merchant-detail">
          <div class="shop-header">
            <el-avatar :size="80" :src="normalizeImageUrl(currentMerchant.avatar, defaultAvatar)">{{ currentMerchant.nickname?.[0] || currentMerchant.username?.[0] }}</el-avatar>
            <div>
              <h3>{{ currentMerchant.nickname || currentMerchant.username }}</h3>
              <el-tag :type="getStatusType(currentMerchant.status)">{{ getStatusText(currentMerchant.status) }}</el-tag>
            </div>
          </div>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商家ID">{{ currentMerchant.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ currentMerchant.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ currentMerchant.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentMerchant.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDateTime(currentMerchant.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, approveUser, disableUser, enableUser } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { defaultAvatar, normalizeImageUrl } from '@/utils/image'

interface Merchant {
  id: number
  username: string
  nickname?: string
  phone?: string
  avatar?: string
  status: number
  createTime: string
}

const loading = ref(false)
const merchantList = ref<Merchant[]>([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const currentMerchant = ref<Merchant | null>(null)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

function getStatusType(status: number) {
  const types: Record<number, string> = { 1: 'success', 0: 'danger', 2: 'warning' }
  return types[status] || 'info'
}

function getStatusText(status: number) {
  const texts: Record<number, string> = { 1: '正常', 0: '禁用', 2: '待审核' }
  return texts[status] || '未知'
}

async function loadMerchants() {
  loading.value = true
  try {
    const res = await getUserList({
      userType: 1,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    const data = res.data
    merchantList.value = (data?.records || data?.list || []).map((u: any) => ({
      id: u.id,
      username: u.username,
      nickname: u.nickname || u.username,
      phone: u.phone || '',
      avatar: u.avatar || '',
      status: u.status,
      createTime: u.createTime || ''
    }))
    total.value = data?.total || 0
  } catch {
    // 请求失败，保持空列表
  } finally {
    loading.value = false
  }
}

function handleView(merchant: Merchant) {
  currentMerchant.value = merchant
  detailDrawerVisible.value = true
}

async function handleAudit(merchant: Merchant, pass: number) {
  const action = pass === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定要${action}该商家的入驻申请吗？`, '审核确认')
  try {
    await approveUser(merchant.id, pass)
    ElMessage.success(`已${action}该商家入驻`)
    loadMerchants()
  } catch {}
}

async function handleToggleStatus(merchant: Merchant, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定要${action}该商家吗？`, '确认操作')
  try {
    if (status === 1) {
      await enableUser(merchant.id)
    } else {
      await disableUser(merchant.id)
    }
    ElMessage.success(`${action}成功`)
    loadMerchants()
  } catch {}
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
.merchant-list-page {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 18px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.shop-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.shop-info {
  flex: 1;
  min-width: 0;
}

.shop-info .name {
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-info .contact {
  font-size: 12px;
  color: var(--color-text-secondary);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.merchant-detail .shop-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.merchant-detail .shop-header h3 {
  margin-bottom: 8px;
}
</style>
